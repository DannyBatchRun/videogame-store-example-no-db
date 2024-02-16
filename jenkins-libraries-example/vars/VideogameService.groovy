def call() {
    println "Hello from VideogameService!"
}

def createJarFile(def PATH) {
    dir("videogame-store-example-no-db/${PATH}") {
        sh("mvn -v")
        sh("mvn clean install")
    }
}

def buildAndPushOnDocker(def PATH, def IMAGE_NAME, def IMAGE_TAG, def passwordEncrypted) {
    dir("videogame-store-example-no-db/${PATH}") {
        sh("docker buildx build . -t ${IMAGE_NAME}")
        sh("docker tag ${IMAGE_NAME} ${params.USERNAME_DOCKERHUB}/${IMAGE_NAME}:${IMAGE_TAG}")
        useAnsibleVault("${passwordEncrypted}", "decrypt")
        sh("docker push ${params.USERNAME_DOCKERHUB}/${IMAGE_NAME}:${IMAGE_TAG}")
        useAnsibleVault("${passwordEncrypted}", "encrypt")
    }
}

def useAnsibleVault(def passwordEncrypted, def choice) {
    dir("/home/daniele/.docker") {
        sh("( set +x; echo ${passwordEncrypted} > passwordFile )")
        sh("ansible-vault ${choice} config.json --vault-password-file passwordFile && rm passwordFile")
    }
}

