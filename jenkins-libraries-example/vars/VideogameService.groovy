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

def copyWorkspace(def PATH, def RELEASE_VERSION) {
    def JAR_FILE
    def NEW_FOLDER = "/home/daniele/releases/release/${RELEASE_VERSION}"
    switch(PATH) {
        case "store-usersubscription-example":
            JAR_FILE = "usersubscription"
            break
        case "store-videogame-products-example":
            JAR_FILE = "videogameproducts"
            break
        case "store-videogamestore-final-example":
            JAR_FILE = "videogamestore"
            break
    }
    sh("mkdir ${NEW_FOLDER}")
    dir("${PATH}/target") {
        sh("sudo cp ${JAR_FILE}.jar /home/daniele/releases/release/${RELEASE_VERSION}")
    }
    println "A backup has been stored in this folder. Please refer to this folder if you lost your data : ${NEW_FOLDER}"
}

def buildAndPushOnDocker(def PATH, def IMAGE_NAME, def IMAGE_TAG, def passwordEncrypted) {
    def USERNAME_DOCKERHUB = "dannybatchrun"
    try {
        dir("${PATH}") {
            sh("docker buildx build . -t ${IMAGE_NAME}")
            sh("docker tag ${IMAGE_NAME} ${USERNAME_DOCKERHUB}/${IMAGE_NAME}:${IMAGE_TAG}")
            useAnsibleVault("${passwordEncrypted}", "decrypt")
            sh("docker push ${USERNAME_DOCKERHUB}/${IMAGE_NAME}:${IMAGE_TAG}")
            useAnsibleVault("${passwordEncrypted}", "encrypt")
        }
    } catch (Exception exc) {
        if(exc.message.contains("certificate signed by unknown authority")) {
            currentBuild.result = "UNSTABLE"
        } else {
            currentBuild.result = "FAILURE"
        }
    }
}

def useAnsibleVault(def passwordEncrypted, def choice) {
    dir("/home/daniele/.docker") {
        sh("( set +x; echo ${passwordEncrypted} > passwordFile )")
        sh("ansible-vault ${choice} config.json --vault-password-file passwordFile && rm passwordFile")
    }
}
