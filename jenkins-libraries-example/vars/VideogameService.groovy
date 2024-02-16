import jenkins.model.Jenkins

static void call() {
    println "VideogameService initialized"
    return this;
}

static void createJarFile(def PATH) {
    dir("videogame-store-example-no-db/${PATH}") {
        sh("mvn -v")
        sh("mvn clean install")
    }
}

static void buildAndPushOnDocker(def PATH, def IMAGE_NAME, def IMAGE_TAG, def passwordEncrypted) {
    dir("videogame-store-example-no-db/${PATH}") {
        sh("docker buildx build . -t ${IMAGE_NAME}")
        sh("docker tag ${IMAGE_NAME} ${params.USERNAME_DOCKERHUB}/${IMAGE_NAME}:${IMAGE_TAG}")
        useAnsibleVault("${passwordEncrypted}", "decrypt")
        sh("docker push ${params.USERNAME_DOCKERHUB}/${IMAGE_NAME}:${IMAGE_TAG}")
        useAnsibleVault("${passwordEncrypted}", "encrypt")
    }
}

static void useAnsibleVault(def passwordEncrypted, def choice) {
    dir("/home/daniele/.docker") {
        sh("( set +x; echo ${passwordEncrypted} > passwordFile )")
        sh("ansible-vault ${choice} config.json --vault-password-file passwordFile && rm passwordFile")
    }
}

