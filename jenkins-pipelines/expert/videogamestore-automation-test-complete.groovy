@Library('jenkins-library-videogame-store')

def service = new VideogameAutomationService().call()

pipeline {
    agent any
    parameters {
        booleanParam(name: 'USERSUBSCRIPTION_TEST', description: "Spunta questa casella se vuoi testare l'applicazione UserSubscription.")
        booleanParam(name: 'VIDEOGAMEPRODUCTS_TEST', description: "Spunta questa casella se vuoi testare l'applicazione VideogameProducts.")
        booleanParam(name: 'VIDEOGAMESTORE_TEST', description: "Spunta questa casella se vuoi testare l'applicazione Videogamestore. ATTENZIONE! Se selezionato, verranno lanciati anche i test di UserSubscription e VideogameStore.")
    }
    stages {
        stage('VideogameStore Check') {
            steps {
                script {
                    if (params.VIDEOGAMESTORE_TEST && (!params.USERSUBSCRIPTION_TEST || !params.VIDEOGAMEPRODUCTS_TEST)) {
                        USERSUBSCRIPTION_TEST = true
                        VIDEOGAMEPRODUCTS_TEST = true
                    }
                    sleep 300
                    echo "**** CHECK VERSION OF NPM ****"
                    sh("npm version")
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
                    service.prepareEndpoints("usersubscription")
                    service.installDependenciesNodeJs("usersubscription")
                    echo "** ADDING FOUR USERS FOR A SUBSCRIPTION MONHLY ** SLEEP FOR 20 SECONDS"
                    sleep 20
                    service.runTestCucumber("usersubscription", "postrequestmonthly")
                    echo "** ADDING FOUR USERS FOR A SUBSCRIPTION ANNUAL ** SLEEP FOR 20 SECONDS"
                    sleep 20
                    service.runTestCucumber("usersubscription", "postrequestannual")
                    service.runTestCucumber("usersubscription", "getrequest")
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
                    service.prepareEndpoints("videogameproducts")
                    service.installDependenciesNodeJs("videogameproducts")
                    service.runTestCucumber("videogameproducts", "postrequest")
                    echo "*** PRODUCT ID TO BE DELETED : 1 *** SLEEP FOR 20 SECONDS"
                    sleep 20
                    service.runTestCucumber("videogameproducts", "deleterequest")
                    service.runTestCucumber("videogameproducts", "getrequest")
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
                    service.prepareEndpoints("videogamestore")
                    service.installDependenciesNodeJs("videogamestore")
                    echo "*** SYNCRONIZING DATABASES OF USERSUBSCRIPTION AND VIDEOGAMEPRODUCTS ***"
                    service.runTestCucumber("videogamestore", "synchronize")
                    service.runTestCucumber("videogamestore", "postrequest")
                    echo "*** WAITING FOR 20 SECONDS AFTER SEND GETREQUEST TEST ***"
                    sleep 20
                    service.runTestCucumber("videogamestore", "getrequest")
                }
            }
        }
    }
    
    post {
        success {
            script {
                echo "Pipeline Success"
            }
            cleanWs()
        }
        failure {
            script {
                echo "Pipeline Failure"
                sh("minikube stop")
            }
            cleanWs()
        }
    }
}
