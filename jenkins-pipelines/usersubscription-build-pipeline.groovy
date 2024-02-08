pipeline {
    agent any

    stages {
        stage('Clone Repository') {
            steps {
                script {
                    sh("git clone https://github.com/VintageMachine/videogame-store-example-no-db.git")
                    dir("videogame-store-example-no-db") {
                        sh("git checkout ${BRANCH_NAME}")
                    }
                } 
            }
        }
        stage('Maven Stage') {
            steps {
                script {
                    dir("videogame-store-example-no-db/store-usersubscription-example") {
                        sh("mvn -v")
                        sh("mvn clean install")
                    }
                } 
            }
        }
        stage('Docker Stage') {
            steps {
                script {
                    dir("videogame-store-example-no-db/store-usersubscription-example") {
                        sh("docker version")
                        sh("docker build -t ${IMAGE_NAME} .")
                        sh("docker tag ${IMAGE_NAME} ${USERNAME_DOCKERHUB}/${IMAGE_NAME}:${IMAGE_TAG}")
                        sh("docker push ${USERNAME_DOCKERHUB}/${IMAGE_NAME}:${IMAGE_TAG}")
                    }
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
