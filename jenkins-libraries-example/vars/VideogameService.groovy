class VideogameService {
    static void call(script) {
        script.echo "Hello from VideogameService!"
    }

    static void createJarFile(script, String PATH) {
        script.dir("videogame-store-example-no-db/${PATH}") {
            script.sh("mvn -v")
            script.sh("mvn clean install")
        }
    }

    static void buildAndPushOnDocker(script, String PATH, String IMAGE_NAME, String IMAGE_TAG, String passwordEncrypted) {
        script.dir("videogame-store-example-no-db/${PATH}") {
            script.sh("docker buildx build . -t ${IMAGE_NAME}")
            script.sh("docker tag ${IMAGE_NAME} ${script.params.USERNAME_DOCKERHUB}/${IMAGE_NAME}:${IMAGE_TAG}")
            useAnsibleVault(script, "${passwordEncrypted}", "decrypt")
            script.sh("docker push ${script.params.USERNAME_DOCKERHUB}/${IMAGE_NAME}:${IMAGE_TAG}")
            useAnsibleVault(script, "${passwordEncrypted}", "encrypt")
        }
    }

    static void useAnsibleVault(script, String passwordEncrypted, String choice) {
        script.dir("/home/daniele/.docker") {
            script.sh("( set +x; echo ${passwordEncrypted} > passwordFile )")
            script.sh("ansible-vault ${choice} config.json --vault-password-file passwordFile && rm passwordFile")
        }
    }
}
