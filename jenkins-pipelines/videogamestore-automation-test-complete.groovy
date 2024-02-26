pipeline {
    agent any
    parameters {
        booleanParam(name: 'USERSUBSCRIPTION_TEST', description: "Spunta questa casella se vuoi testare l'applicazione UserSubscription.")
        booleanParam(name: 'VIDEOGAMEPRODUCTS_TEST', description: "Spunta questa casella se vuoi testare l'applicazione VideogameProducts.")
        booleanParam(name: 'VIDEOGAMESTORE_TEST', description: "Spunta questa casella se vuoi testare l'applicazione Videogamestore.")
    }
    stages {
        stage('Local Forwarding') {
            steps {
                script {
                    forwardKubernetesPort("usersubscription")
                    forwardKubernetesPort("videogameproducts")
                    forwardKubernetesPort("videogamestore")
                }
            }
        }
        stage('Test UserSubscription') {
            when {
                expression {
                    return params.USERSUBSCRIPTION_TEST
                }
            }
            steps {
                script {
                    installDependenciesNodeJs("usersubscription")
                    echo "** ADDING FOUR USERS FOR A SUBSCRIPTION MONHLY ** SLEEP FOR 2 MINUTES"
                    sleep 120
                    runTestCucumber("usersubscription", "postrequestmonthly")
                    echo "** ADDING FOUR USERS FOR A SUBSCRIPTION ANNUAL ** SLEEP FOR 2 MINUTES"
                    sleep 120
                    runTestCucumber("usersubscription", "postrequestannual")
                    runTestCucumber("usersubscription", "getrequest")
                }
            }
        }
        stage('Test VideogameProducts') {
            when {
                expression {
                    return params.VIDEOGAMEPRODUCTS_TEST
                }
            }
            steps {
                script {
                    installDependenciesNodeJs("videogameproducts")
                    runTestCucumber("videogameproducts", "postrequest")
                    echo "*** PRODUCT ID TO BE DELETED : 1 *** SLEEP FOR 2 MINUTES"
                    sleep 120
                    runTestCucumber("videogameproducts", "deleterequest")
                    runTestCucumber("videogameproducts", "getrequest")
                }
            }
        }
        stage('Test VideogameStore') {
            when {
                expression {
                    return params.VIDEOGAMESTORE_TEST
                }
            }
            steps {
                script {
                    installDependenciesNodeJs("videogamestore")
                    echo "*** SYNCRONIZING DATABASES OF USERSUBSCRIPTION AND VIDEOGAMEPRODUCTS ***"
                    runTestCucumber("videogamestore", "synchronize")
                    runTestCucumber("videogamestore", "postrequest")
                    runTestCucumber("videogamestore", "getrequest")
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

def installDependenciesNodeJs(def microservice) {
    sh("npm version")
    switch("${microservice}") {
        case "usersubscription":
            installIntoDirectory("store-usersubscription-example","postrequestmonthly")
            installIntoDirectory("store-usersubscription-example","postrequestannual")
            installIntoDirectory("store-usersubscription-example","getrequest")
        break
        case "videogameproducts":
            installIntoDirectory("store-videogame-products-example","postrequest")
            installIntoDirectory("store-videogame-products-example","getrequest")
            installIntoDirectory("store-videogame-products-example","deleterequest")
        break
        case "videogamestore":
            installIntoDirectory("store-videogamestore-final-example","synchronize")
            installIntoDirectory("store-videogamestore-final-example","postrequest")
            installIntoDirectory("store-videogamestore-final-example","getrequest")
        break
    }
}


def forwardKubernetesPort(def microservice) {
    def servicePort
    switch("${microservice}") {
        case "usersubscription":
            servicePort = "8081"
        break
        case "videogameproducts":
            servicePort = "8100"
        break
        case "videogamestore":
            servicePort = "8080"
        break
    }
    def podName = sh(script: "kubectl get pods -l \"app.kubernetes.io/instance=${microservice}\" -o jsonpath='{.items[0].metadata.name}'", returnStdout: true).trim()
    echo "Pod Name ${microservice}: ${podName}"
    sh("kubectl port-forward ${podName} ${servicePort}:${servicePort} &")
}

def installIntoDirectory(def path, def testType) {
    dir ("${path}/cucumber-auto/${testType}") {
        sh("npm install --save @cucumber/cucumber axios pactum")
        echo "Dependencies installed for ${testType}"
    }
}

def runTestCucumber(def microservice, def testType) {
    def path
    switch("${microservice}") {
        case "usersubscription":
            path = "store-usersubscription-example"
        break
        case "videogameproducts":
            path = "store-videogame-products-example"
        break
        case "videogamestore":
            path = "store-videogamestore-final-example"
        break
    }
    println "*** RUNNING TEST ${microservice.toUpperCase()} : ${testType.toUpperCase()} ***"
    dir ("${path}/cucumber-auto/${testType}") {
        sh("npm test")
    }
    println "*** ${microservice.toUpperCase()} : ${testType.toUpperCase()} COMPLETED SUCCESSFULLY ***"
}
