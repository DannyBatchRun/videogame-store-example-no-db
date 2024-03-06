def call() {
    println "VideogameServiceInfrastructure initialized"
    return this;
}

def createHelmManifest(def microservice) {
    sh("kubectl create namespace ${microservice} || true")
    dir("helm-integration/${microservice}") {
        sh("helm package .")
        def pkg = sh(script: 'ls *.tgz', returnStdout: true).trim()
        sh("helm install ${microservice} ./${pkg} --set image.repository=index.docker.io/dannybatchrun/${microservice},image.tag=1.0.0,service.type=NodePort -n ${microservice}")
        sh("helm get manifest ${microservice}")
    }
}

def executeCommand(def command) {
    try {
        sh(command)
        echo "${command} command executed successfully"
    } catch (Exception e) {
        error("${command} command failed.")
    }
}

def controlContext(def requested) {
    def currentContext = sh(script: 'kubectl config current-context', returnStdout: true).trim()
    if (!currentContext.equals(requested)) {
        sh "kubectl config use-context ${requested}"
        println "Switched to ${requested} context."
    } else {
        println "Already in ${requested} context."
    }
}

def cleanLocalInfrastructures() {
    def result = sh(script: 'helm list -q | wc -l', returnStdout: true).toString().trim()
    result = result.toInteger()
    if (result > 0) {
        sh("helm list -q | xargs -n 1 helm uninstall")
    } else {
        println "No Helm releases found."
    }
    sh("docker rmi \$(docker images -q dannybatchrun/usersubscription) --force || true")
    sh("docker rmi \$(docker images -q dannybatchrun/videogameproducts) --force || true")
    sh("docker rmi \$(docker images -q dannybatchrun/videogamestore) --force || true")
    def deployments = sh(script: "kubectl get deployments --all-namespaces", returnStdout: true).trim()
    if (!deployments.contains("No resources found")) {
        sh("kubectl delete deployments --all --all-namespaces")
    } else {
        println "No deployments found"
    }
    sh("kubectl delete namespace usersubscription --ignore-not-found")
}

