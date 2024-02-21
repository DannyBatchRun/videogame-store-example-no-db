@Library('jenkins-library-videogame-store') 

def SERVICE_PORT

def deployService = new VideogameServiceDeploy().call()

pipeline {
    agent any
    parameters {
        choice(name: 'IMAGE_NAME', choices: ['usersubscription', 'videogameproducts', 'videogamestore'], description: 'Scegli il microservizio richiesto per il deploy')
        string(name: 'IMAGE_VERSION', defaultValue: 'latest', description: 'Inserisci il versionamento per ogni microservizio. Esempio : 1.0.0')
        booleanParam(name: 'DEPLOY_ALL', description: "ATTENZIONE! Seleziona questa casella solo se intendi effettuare il deploy di tutti i microservizi. La versione dovrà esistere per tutti i microservizi.")
    }
    stages {
        stage('Setup Service Port') {
            steps {
                script {
                    SERVICE_PORT = params.DEPLOY_ALL ? false : deployService.getServicePort("${params.IMAGE_NAME}")
                }
            }
        }
        stage('Docker Stage') {
            steps {
                script {
                    sh("docker version")
                    deployService.pullDockerImage("${params.DEPLOY_ALL}","${IMAGE_NAME}","${IMAGE_VERSION}")
                } 
            }
        }
        stage('Replace Image Deployment') {
            steps {
                script {
                    if(!params.DEPLOY_ALL) {
                        deployService.upgradeHelmDeployment("${params.IMAGE_NAME}","${params.IMAGE_VERSION}","${SERVICE_PORT}")
                    } else if (params.DEPLOY_ALL) {
                        deployService.upgradeHelmDeployment("usersubscription","${params.IMAGE_VERSION}","8081")
                        deployService.upgradeHelmDeployment("videogameproducts","${params.IMAGE_VERSION}","8100")
                        deployService.upgradeHelmDeployment("videogamestore","${params.IMAGE_VERSION}","8080")
                    }
                } 
            }
        }
        stage('Check Pods') {
            steps {
                script {
                    sh("kubectl get pods -n default")
                } 
            }
        }
    }
    post {
        success {
            echo "Pipeline Success"
            cleanWs()
        }
        failure {
            echo "Pipeline Failure"
            cleanWs()
        }
    }
}
