pipeline {
    agent any

    stages {
        stage('Clone Repository') {
            steps {
                script {
                    sh("git clone https://github.com/VintageMachine/videogame-store-example-no-db.git") 
                }
            }
        }
        
        stage('Node Dependencies') {
            steps {
                script {
                    sh("npm version")
                    dir("videogame-store-example-no-db/store-videogame-products-example/cucumber/postrequest") {
                        sh("npm install --save @cucumber/cucumber axios pactum")
                        echo "Dependencies installed for PostRequest"
                    }
                    dir("videogame-store-example-no-db/store-videogame-products-example/cucumber/getrequest") {
                        sh("npm install --save @cucumber/cucumber axios pactum")
                        echo "Dependencies installed for GetRequest"
                    }
                }
            }
        }
        
        stage('Check Parameters') {
            steps {
                script {
                    dir("videogame-store-example-no-db/store-videogame-products-example/cucumber/postrequest") {
                        PRODUCTID_TEST = PRODUCTID_TEST.replaceAll("[^0-9]", "")
                        PRICE_PRODUCT_TEST = PRICE_PRODUCT_TEST.replaceAll("[^0-9\\.]", "")
                        PRODUCTID_TEST = PRODUCTID_TEST.equals("") ? "1" : PRODUCTID_TEST
                        PRICE_PRODUCT_TEST = PRICE_PRODUCT_TEST.equals("") ? "50.0" : PRICE_PRODUCT_TEST
                    }
                }
            }
        }
        
        stage('Test Run PostScript') {
            steps {
                script {
                    dir("videogame-store-example-no-db/store-videogame-products-example/cucumber/postrequest") {
                        sh("sed -i 's/INSERT_PRODUCT_ID/'\"${PRODUCTID_TEST}\"'/g' features/add_videogame_product.feature")
                        sh("sed -i 's/INSERT_NAME_HERE/'\"${NAME_PRODUCT_TEST}\"'/g' features/add_videogame_product.feature")
                        sh("sed -i 's/INSERT_TYPE_HERE/'\"${TYPE_PRODUCT_TEST}\"'/g' features/add_videogame_product.feature")
                        sh("sed -i 's/INSERT_PRICE_HERE/'\"${PRICE_PRODUCT_TEST}\"'/g' features/add_videogame_product.feature")
                        sh("npm test")
                    }
                }
            }
        }
        
        stage('Test Run GetScript') {
            steps {
                script {
                    dir("videogame-store-example-no-db/store-videogame-products-example/cucumber/getrequest") {
                        sh("npm test")
                    }
                }
            }
        }
        
        stage ('Test Run DeleteScript') {
            when {
                expression {
                    return params.DELETE_PRODUCT
                }
            }
            steps {
                script {
                    dir("videogame-store-example-no-db/store-videogame-products-example/cucumber/deleterequest") {
                        DELETE_PRODUCTID = DELETE_PRODUCTID.replaceAll("[^0-9]","")
                        DELETE_PRODUCTID = DELETE_PRODUCTID.equals("") ? "1" : DELETE_PRODUCTID
                        sh("npm install --save @cucumber/cucumber axios pactum")
                        echo "Dependencies installed for DeleteRequest"
                        sh("sed -i 's/INSERT_ID_HERE/'\"${DELETE_PRODUCTID}\"'/g' features/remove_videogame_product.feature")
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
