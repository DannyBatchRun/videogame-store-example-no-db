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
        sh("helm get manifest ${microservice} -n ${microservice}")
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

def installOrUpgradeHelmManifest(def microservice, def imageTag, def servicePort) {
    def helmList = sh(script: "helm list --short -n ${microservice} || true", returnStdout: true).trim()
    def isPresent = (helmList.contains("${microservice}")) ? true : false
    dir("helm-integration/${microservice}") {
        sh("helm package .")
        if(!isPresent) {
            def pkg = sh(script: 'ls *.tgz', returnStdout: true).trim()
            sh("helm install ${microservice} ./${pkg} --set image.repository=index.docker.io/dannybatchrun/${microservice},image.tag=${imageTag},image.pullPolicy=Always,service.port=${servicePort},livenessProbe.httpGet.path=/health,livenessProbe.httpGet.port=${servicePort},livenessProbe.initialDelaySeconds=30,readinessProbe.httpGet.path=/health,readinessProbe.httpGet.port=${servicePort},readinessProbe.initialDelaySeconds=30,service.type=LoadBalancer -n ${microservice}")        
        } else if (isPresent) {
            def chartVersion = imageTag
            sh("sed -i 's/^version: 0.1.0/version: '\"${chartVersion}\"'/' Chart.yaml")
            chartVersion = chartVersion.replaceAll(/[^0-9.]/, '')
            sh("kubectl scale --replicas=0 deployment/${microservice} -n ${microservice}")
            sh("helm upgrade ${microservice} . --set image.repository=index.docker.io/dannybatchrun/${microservice},image.tag=${imageTag},image.pullPolicy=Always,service.port=${servicePort},livenessProbe.httpGet.path=/health,livenessProbe.httpGet.port=${servicePort},livenessProbe.initialDelaySeconds=30,readinessProbe.httpGet.path=/health,readinessProbe.httpGet.port=${servicePort},readinessProbe.initialDelaySeconds=30,service.type=LoadBalancer -n ${microservice}")
            sh("kubectl scale --replicas=1 deployment/${microservice} -n ${microservice}")
        }
        def networkPolicyExists = sh(script: "kubectl get networkpolicy allow-all -n ${microservice} --ignore-not-found || true", returnStdout: true).trim()
        def command = networkPolicyExists ? "echo 'NetworkPolicy allow-all already exists in namespace ${microservice}'" : "kubectl apply -f networkpolicy.yaml -n ${microservice}"
        sh(script: command)
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
    println "**** Deleting Helm Manifests ****"
    sh("helm uninstall usersubscription -n usersubscription || true")
    sh("helm uninstall videogameproducts -n videogameproducts || true")
    sh("helm uninstall videogamestore -n videogamestore || true")
    println "**** Deleting Docker Images ****"
    sh("docker rmi \$(docker images -q dannybatchrun/usersubscription) --force || true")
    sh("docker rmi \$(docker images -q dannybatchrun/videogameproducts) --force || true")
    sh("docker rmi \$(docker images -q dannybatchrun/videogamestore) --force || true")
    def deployments = sh(script: "kubectl get deployments --all-namespaces", returnStdout: true).trim()
    if (!deployments.contains("No resources found")) {
        sh("kubectl delete deployments --all --all-namespaces")
    } else {
        println "No deployments found"
    }
}
