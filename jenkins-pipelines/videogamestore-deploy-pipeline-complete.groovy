def SERVICE_PORT

pipeline {
    agent any
    parameters {
        choice(name: 'IMAGE_NAME', choices: ['usersubscription', 'videogameproducts', 'videogamestore'], description: 'Scegli il microservizio richiesto per il deploy')
        string(name: 'IMAGE_VERSION', defaultValue: 'latest', description: 'Inserisci il versionamento per ogni microservizio. Esempio : 1.0.0')
        booleanParam(name: 'DEPLOY_ALL', description: "ATTENZIONE! Seleziona questa casella solo se intendi effettuare il deploy di tutti i microservizi. La versione dovrà esistere per tutti i microservizi.")
    }
    stages {
        stage('Setup Parameters') {
            steps {
                script {
                    SERVICE_PORT = params.DEPLOY_ALL ? false : getServicePort("${params.IMAGE_NAME}")
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
                    if(!params.DEPLOY_ALL) {
                        upgradeHelmDeployment("${params.IMAGE_NAME}","${params.IMAGE_VERSION}","${SERVICE_PORT}")
                    } else if (params.DEPLOY_ALL) {
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
