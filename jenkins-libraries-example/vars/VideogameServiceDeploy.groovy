def call() {
    println "VideogameServiceDeploy initialized"
    return this;
}

def getServicePort(def microservice) {
    def servicePort 
    switch (microservice) {
        case "usersubscription":
            servicePort = "8090"
            break
        case "videogameproducts":
            servicePort = "8100"
            break
        case "videogamestore":
            servicePort = "8080"
            break
        default:
            servicePort = "80"
    }
    return servicePort
}

def pullDockerImage(def deployAll, def imageName, def imageVersion) {
    if (deployAll) {
        sh("docker pull index.docker.io/dannybatchrun/usersubscription:${imageVersion}")
        sh("docker pull index.docker.io/dannybatchrun/videogameproducts:${imageVersion}")
        sh("docker pull index.docker.io/dannybatchrun/videogamestore:${imageVersion}")
    } else if (!deployAll) {
        sh("docker pull index.docker.io/dannybatchrun/${imageName}:${imageVersion}")
    }
}

def upgradeHelmDeployment(def imageName, def imageTag, def servicePort) {
    def chartVersion = imageTag
    chartVersion = chartVersion.replaceAll(/[^0-9.]/, '')
    echo "**** Chart Version of Helm : ${chartVersion} ****"
    dir("helm-integration/${imageName}") {
        sh("sed -i 's/^version: 0.1.0/version: '\"${chartVersion}\"'/' Chart.yaml")
        sh("helm package .")
        sh("kubectl scale --replicas=0 deployment/${imageName} -n ${imageName}")
        sh("helm upgrade ${imageName} . --set image.repository=index.docker.io/dannybatchrun/${imageName},image.tag=${imageTag},image.pullPolicy=Always,service.port=${servicePort},livenessProbe.httpGet.path=/health,livenessProbe.httpGet.port=${servicePort},service.type=NodePort -n ${imageName}")
        sh("kubectl scale --replicas=1 deployment/${imageName} -n ${imageName}")
    }
}
