class VideogameService {
    def script

    static VideogameService call(script) {
        def instance = new VideogameService()
        instance.script = script
        script.echo "Hello from VideogameService!"
        return instance
    }

    void createJarFile(String PATH) {
        script.dir("videogame-store-example-no-db/${PATH}") {
            script.sh("mvn -v")
            script.sh("mvn clean install")
        }
    }

    void buildAndPushOnDocker(String PATH, String IMAGE_NAME, String IMAGE_TAG, String passwordEncrypted) {
        script.dir("videogame-store-example-no-db/${PATH}") {
            script.sh("docker buildx build . -t ${IMAGE_NAME}")
            script.sh("docker tag ${IMAGE_NAME} ${script.params.USERNAME_DOCKERHUB}/${IMAGE_NAME}:${IMAGE_TAG}")
            useAnsibleVault("${passwordEncrypted}", "decrypt")
            script.sh("docker push ${script.params.USERNAME_DOCKERHUB}/${IMAGE_NAME}:${IMAGE_TAG}")
            useAnsibleVault("${passwordEncrypted}", "encrypt")
        }
    }

    void useAnsibleVault(String passwordEncrypted, String choice) {
        script.dir("/home/daniele/.docker") {
            script.sh("( set +x; echo ${passwordEncrypted} > passwordFile )")
            script.sh("ansible-vault ${choice} config.json --vault-password-file passwordFile && rm passwordFile")
        }
    }
}
