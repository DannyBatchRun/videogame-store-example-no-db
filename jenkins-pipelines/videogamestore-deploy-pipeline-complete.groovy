def SERVICE_PORT

pipeline {
    agent any
    parameters {
        choice(name: 'IMAGE_NAME', choices: ['usersubscription', 'videogameproducts', 'videogamestore'], description: 'Scegli il microservizio richiesto per il deploy')
        string(name: 'IMAGE_VERSION', defaultValue: 'latest', description: 'Inserisci il versionamento per ogni microservizio. Esempio : 1.0.0')
        booleanParam(name: 'DEPLOY_ALL', description: '''ATTENZIONE! Seleziona questa casella solo se intendi effettuare il deploy di tutti i microservizi.
        Assicurati che il versionamento è esistente per ogni microservizio.''')
    }
    stages {
        stage('Setup Parameters') {
            steps {
                script {
                    SERVICE_PORT = params.DEPLOY_ALL ? false : getServicePort("${params.IMAGE_NAME}")
                    if(!${params.DEPLOY_ALL}) {
                        checkTagExists("${params.IMAGE_NAME}","${params.IMAGE_VERSION}")
                    } else if (${params.DEPLOY_ALL}) {
                        checkTagExists("usersubscription","${params.IMAGE_VERSION}")
                        checkTagExists("videogameproducts","${params.IMAGE_VERSION}")
                        checkTagExists("videogamestore","${params.IMAGE_VERSION}")
                    }
                    //To be removed after merge
                    sh("git checkout helmIntegration")
                }
            }
        }
        stage('Docker Stage') {
            steps {
                script {
                    sh("docker version")
                    pullDockerImage("${params.DEPLOY_ALL}","${IMAGE_NAME}","${IMAGE_VERSION}")
                } 
            }
        }
        stage('Replace Image Deployment') {
            steps {
                script {
                    if(!${params.DEPLOY_ALL}) {
                        upgradeHelmDeployment("${params.IMAGE_NAME}","${params.IMAGE_VERSION}","${SERVICE_PORT}")
                    } else if (${params.DEPLOY_ALL}) {
                        upgradeHelmDeployment("usersubscription","${params.IMAGE_VERSION}","8081")
                        upgradeHelmDeployment("videogameproducts","${params.IMAGE_VERSION}","8100")
                        upgradeHelmDeployment("videogamestore","${params.IMAGE_VERSION}","8080")
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
        always {
            cleanWs()
        }
    }
}

def getServicePort(def microservice) {
    def servicePort 
    switch (microservice) {
        case "usersubscription":
            servicePort = "8081"
            break
        case "videogameproducts":
                servicePort = "8100"
            break
        case "videogamestore":
            servicePort = "8080"
            break
        default:
            servicePort = "80"
    }
    return servicePort
}

def checkTagExists(String repository, String tag) {
    withCredentials([string(credentialsId: 'docker_password', variable: 'DOCKER_PASSWORD')]) {
        sh("echo ${DOCKER_PASSWORD} | docker login -u ${USERNAME_DOCKERHUB} --password-stdin")
        def command = "curl -s https://registry.hub.docker.com/v2/dannybatchrun/${repository}/tags/list"
        println("Running command: ${command}")
        def result = sh(script: command, returnStdout: true).trim()
        println("Command output: ${result}")
        if (result.contains("\"${tag}\"")) {
            println("IMAGE_VERSION ${tag} found for repository ${repository}")
        } else {
            error("IMAGE_VERSION ${tag} not found for repository ${repository}")
        }
        sh("docker logout")
    }
}



def pullDockerImage(def deployAll, def imageName, def imageVersion) {
    if (deployAll) {
        sh("docker pull index.docker.io/dannybatchrun/usersubscription:${imageVersion}")
        sh("docker pull index.docker.io/dannybatchrun/videogameproducts:${imageVersion}")
        sh("docker pull index.docker.io/dannybatchrun/videogamestore:${imageVersion}")
    } else if (!deployAll) {
        sh("docker pull index.docker.io/dannybatchrun/${imageName}:${imageVersion}")
    }
}

def upgradeHelmDeployment(def imageName, def imageTag, def servicePort) {
    dir("helm-integration/${imageName}") {
        sh("sed -i 's/^version: 0.1.0/version: '\"\${imageName}\"'/' Chart.yaml")
        sh("helm package .")
        sh("kubectl scale --replicas=0 deployment/${imageName}")
        sh("helm upgrade ${imageName} . --set image.repository=index.docker.io/dannybatchrun/${imageName},image.tag=${imageTag},image.pullPolicy=Always,service.port=${servicePort},livenessProbe.httpGet.path=/health,livenessProbe.httpGet.port=${servicePort}")
        sh("kubectl scale --replicas=1 deployment/${imageName}")
    }
}
