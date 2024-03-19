pipeline {
    agent any

    stages {
        stage('Clone Repository') {
            steps {
                script {
                    sh("git clone https://github.com/DannyBatchRun/videogame-store-example-no-db.git") 
                }
            }
        }
        
        stage('Node Dependencies') {
            steps {
                script {
                    sh("npm version")
                    dir("videogame-store-example-no-db/store-usersubscription-example/cucumber/postrequest") {
                        sh("npm install --save @cucumber/cucumber axios pactum")
                        echo "Dependencies installed for PostRequest"
                    }
                    dir("videogame-store-example-no-db/store-usersubscription-example/cucumber/getrequest") {
                        sh("npm install --save @cucumber/cucumber axios pactum")
                        echo "Dependencies installed for GetRequest"
                    }
                }
            }
        }
        
        stage('Test Run PostScript') {
            steps {
                script {
                    dir("videogame-store-example-no-db/store-usersubscription-example/cucumber/postrequest") {
                        sh("sed -i 's/INSERT_NAME_HERE/'\"${NAME_TEST}\"'/g' features/add_monthly_subscription.feature")
                        sh("sed -i 's/INSERT_SURNAME_HERE/'\"${SURNAME_TEST}\"'/g' features/add_monthly_subscription.feature")
                        sh("npm test")
                    }
                }
            }
        }
        
        stage('Test Run GetScript') {
            steps {
                script {
                    dir("videogame-store-example-no-db/store-usersubscription-example/cucumber/getrequest") {
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
