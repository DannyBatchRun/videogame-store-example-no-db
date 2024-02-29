def call() {
    println "VideogameAutomationService initialized"
    return this;
}

def installDependenciesNodeJs(def microservice) {
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

def installIntoDirectory(def path, def testType) {
    dir ("${path}/cucumber-auto/${testType}") {
        sh("npm install --save @cucumber/cucumber axios pactum")
        echo "Dependencies installed for ${testType}"
    }
}

def forceForwardIfRequired(def microservice, def servicePort) {
    def isNotForwarded = true
    while (isNotForwarded) {
        def responseCode = sh(script: "curl -s -o /dev/null -w \"%{http_code}\" http://localhost:${servicePort}/health", returnStdout: true).trim()
        if (responseCode != '200') {
            println "Service ${microservice} is not responding. Forwarding port again and cleaning up old forward..."
            forwardKubernetesPort("${microservice}", "restart")
        } else {
            println "Service ${microservice} is running."
            isNotForwarded = false
        }
    }
}

def forwardKubernetesPort(def microservice, def choice) {
    def servicePort
    switch("${microservice}") {
        case "usersubscription":
            servicePort = "8090"
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
    switch("${choice}") {
        case "open":
            sh("nohup kubectl port-forward ${podName} ${servicePort}:${servicePort} > ${microservice}output.log 2>&1 &")
            echo "Waiting for a few seconds before continue."
            sleep 20
            echo "Checking if the pod is in running..."
            forceForwardIfRequired("${microservice}","${servicePort}")
        break
        case "close":
            sh("pgrep -f 'kubectl port-forward ${podName}' | xargs kill")
        break
        case "restart":
            sh("rm ${microservice}output.log || true")
            sh("nohup kubectl port-forward ${podName} ${servicePort}:${servicePort} > ${microservice}output.log 2>&1 &")
        break
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
