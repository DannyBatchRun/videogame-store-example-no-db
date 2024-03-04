def call() {
    println "VideogameAutomationService initialized"
    return this;
}


def installIntoDirectory(def path, def testType) {
    dir ("${path}/cucumber-auto/${testType}") {
        sh("npm install --save @cucumber/cucumber axios pactum")
        echo "Dependencies installed for ${testType}"
    }
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

def retryForward(def microservice, def servicePort, def podName) {
    sh("kill \$(cat ${microservice}output.txt) && rm ${microservice}output.txt")
    sh('nohup kubectl port-forward ' + podName + ' ' + servicePort + ':' + servicePort + ' & echo $! > ' + microservice + 'output.txt')
    sleep 20
}

def forceForwardIfRequired(def microservice, def servicePort, def podName) {
    def isNotForwarded = true
    while (isNotForwarded) {
        def responseCode = sh(script: "curl -s -o /dev/null -w \"%{http_code}\" http://localhost:${servicePort}/health || true", returnStdout: true).trim()
        if (responseCode != '200') {
            println "Service ${microservice} is not responding. Forwarding port again and cleaning up old forward..."
            retryForward("${microservice}","${servicePort}","${podName}")
        } else {
            println "Service ${microservice} is running."
            isNotForwarded = false
        }
    }
}

def forwardKubernetesPort(def microservice, def servicePort, def choice) {
    def podName = sh(script: 'kubectl get pods | grep ' + microservice + ' | awk \'{print $1}\'', returnStdout: true).trim()
    echo "Pod Name ${microservice}: ${podName}"
    if(choice.equals("open")) {
        sh('nohup kubectl port-forward ' + podName + ' ' + servicePort + ':' + servicePort + ' & echo $! > ' + microservice + 'output.txt')
        echo "Waiting for a few seconds before continue."
        sleep 20
        echo "Checking if the pod is in running..."
        forceForwardIfRequired("${microservice}","${servicePort}","${podName}")
    } else if (choice.equals("close")) {
        sh("kill \$(cat ${microservice}output.txt) && rm ${microservice}output.txt")
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

def prepareSynchronize() {
    def urlSubscription = sh(script: 'minikube service usersubscription --url | head -n 1', returnStdout: true).toString().trim()
    def urlVideogame = sh(script: 'minikube service videogameproducts --url | head -n 1', returnStdout: true).toString().trim()
    dir ("store-videogamestore-final-example/cucumber-auto/synchronize") {
        sh("sed -i 's|ENDPOINT_USERSUBSCRIPTION|'\"${urlSubscription}\"'|g' features/synchronize_all.feature")
        sh("sed -i 's|ENDPOINT_VIDEOGAMEPRODUCTS|'\"${urlVideogame}\"'|g' features/synchronize_all.feature")
    }
}
