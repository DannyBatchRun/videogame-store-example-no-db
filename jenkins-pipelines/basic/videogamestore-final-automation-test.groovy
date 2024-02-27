pipeline {
    agent any

    stages {
        stage('Clone Repository') {
            steps {
                script {
                    sh("git clone https://github.com/DannyBatchRun/videogame-store-example-no-db.git")
                    dir("videogame-store-example-no-db") {
                        sh("git checkout videogameupgrade")    
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
