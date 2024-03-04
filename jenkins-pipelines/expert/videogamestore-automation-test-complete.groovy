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
        stage('Initialization') {
            steps {
                script {
                    if (params.VIDEOGAMESTORE_TEST && (!params.USERSUBSCRIPTION_TEST || !params.VIDEOGAMEPRODUCTS_TEST)) {
                        USERSUBSCRIPTION_TEST = true
                        VIDEOGAMEPRODUCTS_TEST = true
                    }
                    echo "**** FORWARDING MICROSERVICES IN PROGRESS ****"
                    service.forwardKubernetesPort("usersubscription","8090","open")
                    sh("cat usersubscriptionoutput.log || true")
                    service.forwardKubernetesPort("videogameproducts","8100","open")
                    sh("cat videogameproductsoutput.log || true")
                    service.forwardKubernetesPort("videogamestore","8080","open")
                    sh("cat videogamestoreoutput.log || true")
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
                    service.installDependenciesNodeJs("videogamestore")
                    echo "*** SYNCRONIZING DATABASES OF USERSUBSCRIPTION AND VIDEOGAMEPRODUCTS ***"
                    service.prepareSynchronize()
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
                params.USERSUBSCRIPTION_TEST ? service.forwardKubernetesPort("usersubscription","8090","close") : null
                params.VIDEOGAMEPRODUCTS_TEST ? service.forwardKubernetesPort("videogameproducts","8100","close") : null
                params.VIDEOGAMESTORE_TEST ? service.forwardKubernetesPort("videogamestore","8080","close") : null
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
