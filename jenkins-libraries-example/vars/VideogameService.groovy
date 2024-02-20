def call() {
    println "VideogameService initialized"
    return this;
}

def createJarFile(def PATH) {
    dir("${PATH}") {
        sh("mvn -v")
        sh("mvn clean install")
    }
}

def buildAndPushOnDocker(def PATH, def IMAGE_NAME, def IMAGE_TAG, def passwordEncrypted) {
    def USERNAME_DOCKERHUB = "dannybatchrun"
    dir("${PATH}") {
        sh("docker buildx build . -t ${IMAGE_NAME}")
        sh("docker tag ${IMAGE_NAME} ${USERNAME_DOCKERHUB}/${IMAGE_NAME}:${IMAGE_TAG}")
        useAnsibleVault("${passwordEncrypted}", "decrypt")
        sh("docker push ${USERNAME_DOCKERHUB}/${IMAGE_NAME}:${IMAGE_TAG}")
        useAnsibleVault("${passwordEncrypted}", "encrypt")
    }
}

def useAnsibleVault(def passwordEncrypted, def choice) {
    dir("/home/daniele/.docker") {
        sh("( set +x; echo ${passwordEncrypted} > passwordFile )")
        sh("ansible-vault ${choice} config.json --vault-password-file passwordFile && rm passwordFile")
    }
}
