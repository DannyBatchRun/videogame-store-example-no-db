class VideogameService {
    static void call() {
        println "Hello from VideogameService!"
    }

    static void createJarFile(String PATH) {
        dir("videogame-store-example-no-db/${PATH}") {
            sh("mvn -v")
            sh("mvn clean install")
        }
    }

    static void buildAndPushOnDocker(String PATH, String IMAGE_NAME, String IMAGE_TAG, String passwordEncrypted) {
        dir("videogame-store-example-no-db/${PATH}") {
            sh("docker buildx build . -t ${IMAGE_NAME}")
            sh("docker tag ${IMAGE_NAME} ${params.USERNAME_DOCKERHUB}/${IMAGE_NAME}:${IMAGE_TAG}")
            useAnsibleVault("${passwordEncrypted}", "decrypt")
            sh("docker push ${params.USERNAME_DOCKERHUB}/${IMAGE_NAME}:${IMAGE_TAG}")
            useAnsibleVault("${passwordEncrypted}", "encrypt")
        }
    }

    static void useAnsibleVault(String passwordEncrypted, String choice) {
        dir("/home/daniele/.docker") {
            sh("( set +x; echo ${passwordEncrypted} > passwordFile )")
            sh("ansible-vault ${choice} config.json --vault-password-file passwordFile && rm passwordFile")
        }
    }
}
