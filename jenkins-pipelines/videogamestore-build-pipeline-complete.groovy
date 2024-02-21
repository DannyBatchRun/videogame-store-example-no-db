@Library('jenkins-library-videogame-store') 

import java.util.Random

def service = new VideogameService().call()

pipeline {
    agent any
    parameters {
        string(name: 'BRANCH_NAME', defaultValue: 'master', description: 'Inserisci il branch per tutte le immagini di Videogame Store')
        string(name: 'IMAGE_TAG', defaultValue: 'latest', description: 'Inserisci il versionamento per ogni microservizio. Esempio : 1.0.0')
        booleanParam(name: 'TRIGGER_DEPLOY', description: "ATTENZIONE! Seleziona questa casella solo se intendi procedere con il deploy subito dopo la build.")
    }
    stages {
        stage('Checkout Branch') {
            steps {
                script {
                    sh("git checkout ${params.BRANCH_NAME}")
                } 
            }
        }
        stage('Maven Stage') {
            steps {
                script {
                   sh("mvn -v")
                   service.createJarFile("store-usersubscription-example")
                   service.createJarFile("store-videogame-products-example")
                   service.createJarFile("store-videogamestore-final-example")
                } 
            }
        }
        stage('Docker Stage') {
            steps {
                script {
                    sh("docker version")
                    def USERNAME_DOCKERHUB = "dannybatchrun"
                    def random = new Random()
                    def generatedPassword = random.nextInt(999999).toString().padLeft(6, '0')
                    withCredentials([string(credentialsId: 'docker_password', variable: 'DOCKER_PASSWORD')]) {
                        sh("echo ${DOCKER_PASSWORD} | docker login -u ${USERNAME_DOCKERHUB} --password-stdin")
                        service.useAnsibleVault("${generatedPassword}", "encrypt")
                        service.buildAndPushOnDocker("store-usersubscription-example","usersubscription","${params.IMAGE_TAG}", "${generatedPassword}")
                        service.buildAndPushOnDocker("store-videogame-products-example","videogameproducts","${params.IMAGE_TAG}", "${generatedPassword}")
                        service.buildAndPushOnDocker("store-videogamestore-final-example","videogamestore","${params.IMAGE_TAG}", "${generatedPassword}")
                        service.useAnsibleVault("${generatedPassword}", "decrypt")
                        sh("docker logout")
                    }
                }
            }
        }
        stage('Trigger Deploy Pipeline') {
            when {
                expression {
                    return params.TRIGGER_DEPLOY
                }
            }
            steps {
                script {
                    build(job: 'videogame-store-deploy-complete', parameters: [
                        choice(name: 'IMAGE_NAME', choices: ['usersubscription', 'videogameproducts', 'videogamestore'], value: 'usersubscription'),
                        string(name: 'IMAGE_VERSION', value: "${params.IMAGE_TAG}"),
                        booleanParam(name: 'DEPLOY_ALL', value: true)
                    ], wait: false)
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
