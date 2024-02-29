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

def forwardKubernetesPort(def microservice, def choice) {
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
    if(choice.equals("open")) {
        sh("nohup kubectl port-forward ${podName} ${servicePort}:${servicePort} > output.log 2>&1 &")
        echo "Waiting for one minute before continue"
        sleep 60
    } else if(choice.equals("close")) {
        sh("pgrep -f 'kubectl port-forward ${podName}' | xargs kill")
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
