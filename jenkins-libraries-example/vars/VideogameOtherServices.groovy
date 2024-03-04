def call() {
    println "VideogameOtherServices initialized"
    return this;
}

def retryForward(def microservice, def servicePort, def podName) {
    sh("""pgrep -f "kubectl port-forward ${podName}" | xargs kill""")
    sh("rm ${microservice}output.log || true")
    sh("nohup kubectl port-forward ${podName} ${servicePort}:${servicePort} > ${microservice}output.log 2>&1 &")
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
    def podName = sh(script: "kubectl get pods -l \"app.kubernetes.io/instance=${microservice}\" -o jsonpath='{.items[0].metadata.name}'", returnStdout: true).trim()
    echo "Pod Name ${microservice}: ${podName}"
    if(choice.equals("open")) {
        sh("nohup kubectl port-forward ${podName} ${servicePort}:${servicePort} > ${microservice}output.log 2>&1 &")
        echo "Waiting for a few seconds before continue."
        sleep 20
        echo "Checking if the pod is in running..."
        forceForwardIfRequired("${microservice}","${servicePort}","${podName}")
    } else if (choice.equals("close")) {
        sh("""pgrep -f "kubectl port-forward ${podName}" | xargs kill""")
    }
}

