def DEPLOY_GKE

pipeline {
    agent any
    parameters {
        string(name: 'BRANCH_NAME', defaultValue: 'master', description: 'Inserisci il branch per eseguire il test.')
        string(name: 'IMAGE_TAG', defaultValue: '1.0.0', description: 'Inserisci il versionamento. Esempio : 1.0.0')
    }
    stages {
        stage('Check Running Packages') {
            steps {
                script {
                    BUILD_TRIGGER_BY = currentBuild.getBuildCauses()[0].shortDescription + " / " + currentBuild.getBuildCauses()[0].userId
                    currentBuild.displayName = "Build N° ${currentBuild.number}"
                    currentBuild.description = "${BUILD_TRIGGER_BY}\nBranch_Name : ${params.BRANCH_NAME}\nTag Name : ${params.IMAGE_TAG}"
                    executeCommand("helm version")
                    executeCommand("java --version")
                    executeCommand("mvn -v")
                    executeCommand("npm version")
                    def minikubeStatus = sh(script: "minikube status || true", returnStdout: true).trim()
                    minikubeStatus.contains("host: Stopped") ? executeCommand("minikube start") : 'Minikube already started'
                    executeCommand("minikube version")
                }
            }
        }
        stage('Clean Previous Install') {
            steps {
                script {
                    echo "**** Cleaning old builds with Helm and Docker ****"
                    def result = sh(script: 'helm list -q | wc -l', returnStdout: true).toString().trim()
                    result = result.toInteger()
                    (result > 0) ? executeCommand("helm list -q | xargs -n 1 helm delete") : 'No Helm releases found.'
                    executeCommand("docker rmi \$(docker images -q dannybatchrun/usersubscription) --force || true")
                    executeCommand("docker rmi \$(docker images -q dannybatchrun/videogameproducts) --force || true")
                    executeCommand("docker rmi \$(docker images -q dannybatchrun/videogamestore) --force || true")
                    echo "**** Docker Images Pruned ****"
                    def deployments = sh(script: "kubectl get deployments --all-namespaces", returnStdout: true).trim()
                    !deployments.contains("No resources found") ? executeCommand("kubectl delete deployments --all --all-namespaces") : 'No deployments found'
                }
            }
        }
        stage('Helm Install') {
            steps {
                script {
                    echo "**** Creating Three Helm Manifests Empty. It will start in a minute. ****"
                    sleep 60
                    createHelmManifest("usersubscription")
                    createHelmManifest("videogameproducts")
                    createHelmManifest("videogamestore")
                }
            }
        }
        stage('Build and Push on Docker') {
            steps {
                script {
                    echo "*** Pipeline Build in Local is in Running. This Pipeline will continue after finished. ****"
                    build(job: "videogame-store-build-complete", parameters: [
                        string(name: "BRANCH_NAME", value: "${params.BRANCH_NAME}"),
                        string(name: "IMAGE_TAG", value: "${params.IMAGE_TAG}")
                    ], wait: true)
                }
            }
        }
        stage('Replace Images Deployment') {
            steps {
                script {
                    echo "*** Pipeline Deploy in Local is in Running. This Pipeline will continue after finished. ****"
                    build(job: "videogame-store-deploy-complete", parameters: [
                        string(name: "IMAGE_NAME", value: "usersubscription"),
                        string(name: "IMAGE_VERSION", value: "${params.IMAGE_TAG}"),
                        booleanParam(name: "DEPLOY_ALL", value: true)
                    ], wait: true)
                }
            }
        }
        stage('Test Automation') {
            steps {
                script {
                    echo "*** Pipeline Automation Test is in Running. This Pipeline will continue after finished. ***"
                    echo "*** Waiting Containers for start. Sleep for 5 minutes. ***"
                    build(job: "videogame-store-automation-test-complete", parameters: [
                        booleanParam(name: "USERSUBSCRIPTION_TEST", value: true),
                        booleanParam(name: "VIDEOGAMEPRODUCTS_TEST", value: true),
                        booleanParam(name: "VIDEOGAMESTORE_TEST", value: true)
                    ], wait: true)                    
                }
            }
        }
        stage('Deploy on GKE') {
            steps {
                script {
                    def userInput = input(id: 'confirm', message: 'Proceed with GKE deploy?', parameters: [ [$class: 'BooleanParameterDefinition', defaultValue: false, description: 'Click yes to proceed', name: 'Yes']])
                    DEPLOY_GKE = userInput
                    if (DEPLOY_GKE) {
                        echo "**** You Selected Yes. GKE Deploy will start in a minute ****"
                    } else {
                        echo "**** You selected No. Pipeline will complete without GKE deployment. ****"
                        currentBuild.result = 'SUCCESS'
                    }
                }
            }
        }
    }
    post {
        success {
            script {
                echo "**** Pipeline SUCCESS ****"
                def message = DEPLOY_GKE ? "**** GKE Deployment is set to true. Proceeding with deployment. ****" : "**** GKE Deployment is set to false. Aborting deployment. ****"
                echo message
                echo "**** Docker Images ****"
                sh("docker image ls | grep usersubscription")
                sh("docker image ls | grep videogameproducts")
                sh("docker image ls | grep videogamestore")
                sh("helm list --short")
            }
            cleanWs()
        }
        failure {
            script {
                echo "**** Pipeline FAILURE ****"
            }
            cleanWs()
        }
    }
}

def createHelmManifest(def microservice) {
    dir("helm-integration/${microservice}") {
        sh("helm package .")
        def pkg = sh(script: 'ls *.tgz', returnStdout: true).trim()
        sh("helm install ${microservice} ./${pkg} --set image.repository=index.docker.io/dannybatchrun/${microservice},image.tag=1.0.0,service.type=NodePort")
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
