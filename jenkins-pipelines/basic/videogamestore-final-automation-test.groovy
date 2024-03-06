pipeline {
    agent any

    stages {
        stage('Clone Repository') {
            steps {
                script {
                    sh("git clone https://github.com/DannyBatchRun/videogame-store-example-no-db.git")
                    dir("videogame-store-example-no-db") {
                        sh("git checkout newPipelineIntegration")    
                    }
                }
            }
        }
        
        stage('Node Dependencies') {
            steps {
                script {
                    sh("npm version")
                    dir("videogame-store-example-no-db/store-videogamestore-final-example/cucumber/synchronize") {
                        sh("npm install --save @cucumber/cucumber axios pactum")
                        echo "Dependencies installed for Synchronize"
                    }
                    dir("videogame-store-example-no-db/store-videogamestore-final-example/cucumber/postrequest") {
                        sh("npm install --save @cucumber/cucumber axios pactum")
                        echo "Dependencies installed for PostRequest"
                    }
                    dir("videogame-store-example-no-db/store-videogamestore-final-example/cucumber/getrequest") {
                        sh("npm install --save @cucumber/cucumber axios pactum")
                        echo "Dependencies installed for GetRequest"
                    }
                }
            }
        }
        stage('Synchronize Databases Test') {
            steps {
                script {
                    dir("videogame-store-example-no-db/store-videogamestore-final-example/cucumber/synchronize") {
                        def urlSubscription = sh(script: 'minikube service usersubscription --url -n usersubscription | head -n 1', returnStdout: true).toString().trim()
                        def urlVideogame = sh(script: 'minikube service videogameproducts --url -n videogameproducts | head -n 1', returnStdout: true).toString().trim()
                        sh("sed -i 's|ENDPOINT_USERSUBSCRIPTION|'\"${urlSubscription}\"'|g' features/synchronize_all.feature")
                        sh("sed -i 's|ENDPOINT_VIDEOGAMEPRODUCTS|'\"${urlVideogame}\"'|g' features/synchronize_all.feature")
                        sh("npm test")
                    }
                }
            }
        }

        stage('Videogame to Customer Cart Test') {
            steps {
                script {
                    dir("videogame-store-example-no-db/store-videogamestore-final-example/cucumber/postrequest") {
                        sh("sed -i 's/INSERT_VIDEOGAME_HERE/'\"${VIDEOGAME_TEST}\"'/g' features/add_videogame_cart_customer.feature")
                        sh("sed -i 's/INSERT_NAME_HERE/'\"${NAME_CLIENT_TEST}\"'/g' features/add_videogame_cart_customer.feature")
                        sh("sed -i 's/INSERT_SURNAME_HERE/'\"${SURNAME_CLIENT_TEST}\"'/g' features/add_videogame_cart_customer.feature")
                        sh("npm test")
                    }
                }
            }
        }
        
        stage('Test All Carts') {
            steps {
                script {
                    dir("videogame-store-example-no-db/store-videogamestore-final-example/cucumber/getrequest") {
                        sh("npm test")
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
