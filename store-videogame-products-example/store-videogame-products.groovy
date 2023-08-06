pipeline {
    agent any
    
    tools {
        maven "Maven 3.8.6"
    }

    stages {
        stage('Building Jar and Docker Image') {
            steps {
                dir("videogame-store-example-no-db/store-videogame-products-example") {
                    sh "mvn install"
                    sh "docker build -t videogamestore ."
                }
            }
        }

        stage('Creating Docker Container') {
            steps {
                dir("videogame-store-example-no-db/store-videogame-products-example") {
                    sh "docker container create --name VideogameStore -p 8082:8082 videogamestore"
                }
            }
        }
    }
    post {
        always {
            script {
                sh "docker stop VideogameStore"
                sh "docker rm VideogameStore"
                cleanWs()
            }
        }
        
        success {
            echo "All Bulding Successfully for Application VideogameStore"
        } 
        failure {
            echo "All Building Failed for Application VideogameStore"
        }
    }
}