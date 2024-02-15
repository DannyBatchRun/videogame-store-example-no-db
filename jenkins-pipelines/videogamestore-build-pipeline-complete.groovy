pipeline {
    agent any

    stages {
        stage('Clone Repository') {
            steps {
                script {
                    sh("git clone https://github.com/DannyBatchRun/videogame-store-example-no-db.git")
                    dir("videogame-store-example-no-db") {
                        sh("git checkout ${params.BRANCH_NAME}")
                    }
                } 
            }
        }
        stage('Maven Stage') {
            steps {
                script {
                   sh("mvn -v")
                   createJarFile("store-usersubscription-example")
                   createJarFile("store-videogame-products-example")
                   createJarFile("store-videogamestore-final-example")
                } 
            }
        }
        stage('Docker Stage') {
            steps {
                script {
                    sh("docker version")
                    buildAndPushOnDocker("store-usersubscription-example","usersubscription","${params.IMAGE_TAG}")
                    buildAndPushOnDocker("store-videogame-products-example","videogameproducts","${params.IMAGE_TAG}")
                    buildAndPushOnDocker("store-videogamestorefinal-example","videogamestore","${params.IMAGE_TAG}")
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

void createJarFile(def PATH) {
    dir("videogame-store-example-no-db/${PATH}") {
        sh("mvn clean install")
    }
}

//You must have the Plugin "Docker Pipeline" installed on Jenkins.
void buildAndPushOnDocker(def PATH, def IMAGE_NAME, def IMAGE_TAG) {
    docker.withRegistry('https://index.docker.io/v1/', "docker_login") {
        dir("videogame-store-example-no-db/${PATH}") {
            sh("docker build -t ${IMAGE_NAME} .")
            sh("docker tag ${IMAGE_NAME} ${params.USERNAME_DOCKERHUB}/${IMAGE_NAME}:${IMAGE_TAG}")
            sh("docker push ${params.USERNAME_DOCKERHUB}/${IMAGE_NAME}:${IMAGE_TAG}")
        }
    }
}
