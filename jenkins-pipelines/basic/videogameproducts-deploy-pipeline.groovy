def IMAGE_SPLITTED = IMAGE_NAME.split(':')
def IMAGE_REPOSITORY = IMAGE_SPLITTED[0]
def IMAGE_TAG = IMAGE_SPLITTED[1]

pipeline {
    agent any

    stages {
        stage('Check Parameters') {
            steps {
                script {
                    APP_VERSION = APP_VERSION.replaceAll("[^0-9.]", "")
                    if(ENABLE_SERVICE_PORT.equals("Yes")) {
                        SERVICE_PORT = SERVICE_PORT.replaceAll("[^0-9]", "")
                    }
                }
            }
        }

        stage('Docker Stage') {
            steps {
                script {
                    sh("docker pull ${IMAGE_NAME}")
                } 
            }
        }
        stage('Replace Image') {
            steps {
                script {
                    sh("kubectl scale --replicas=0 deployment/videogameproducts -n videogameproducts")
                    if(ENABLE_SERVICE_PORT.equals("Yes")) {
                        sh("helm upgrade videogameproducts ~/videogameproducts/. --version ${APP_VERSION} --set image.repository=${IMAGE_REPOSITORY},image.tag=${IMAGE_TAG},image.pullPolicy=Always,service.port=${SERVICE_PORT},livenessProbe.httpGet.path=/health,livenessProbe.httpGet.port=${SERVICE_PORT},readinessProbe.httpGet.path=/health -n videogameproducts")
                    } else if (ENABLE_SERVICE_PORT.equals("No")) {
                        sh("helm upgrade videogameproducts ~/videogameproducts/. --version ${APP_VERSION} --set image.repository=${IMAGE_REPOSITORY},image.tag=${IMAGE_TAG},image.pullPolicy=Always,livenessProbe.httpGet.path=/health,livenessProbe.httpGet.port=80,readinessProbe.httpGet.path=/health -n videogameproducts")
                    }
                    sh("kubectl scale --replicas=1 deployment/videogameproducts -n videogameproducts")
                } 
            }
        }
        stage('Check Pods') {
            steps {
                script {
                    sh("kubectl get pods -n videogamestore")
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

