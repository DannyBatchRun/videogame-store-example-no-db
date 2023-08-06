pipeline {
    agent any

    tools {
        maven "Maven 3.8.6"
    }

    stages {
        stage('Building Jar and Docker Image') {
            steps {
                dir("videogame-store-example-no-db/store-usersubscription-example") {
                    sh "mvn install"
                    sh "docker build -t usersubscription ."
                }
            }
        }

        stage("Creating a Container Docker") {
            steps {
                dir("videogame-store-example-no-db/store-usersubscription-example") {
                    sh "docker container create --name UserSubscription -p 8081:8081 usersubscription"
                }
            }
        } 
    }

    post {
        always {
            script {
                sh 'docker stop UserSubscription'
                sh 'docker rm UserSubscription'
            }
        }
        success {
            echo "Creating Application Successful : UserSubscription"
        }
        failure {
            echo "Creating Application Failed : UserSubscription"
        }
    }
}