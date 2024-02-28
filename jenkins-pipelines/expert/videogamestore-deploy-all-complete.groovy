def DEPLOY_EKS = false

pipeline {
    agent any
    parameters {
        string(name: 'BRANCH_NAME', defaultValue: 'master', description: 'Inserisci il branch per eseguire il test.')
        string(name: 'IMAGE_TAG', defaultValue: '1.0.0', description: 'Inserisci il versionamento. Esempio : 1.0.0')
        booleanParam(name: 'CLEAN_ALL', description: "ATTENZIONE! Seleziona questa casella solo intendi cancellare eventuali installazioni in locale.")
    }
    stages {
        stage('Check Running Packages') {
            steps {
                script {
                    checkPackageInstalled("minikube version")
                    checkPackageInstalled("helm version")
                    checkPackageInstalled("java --version")
                    checkPackageInstalled("mvn -v")
                    checkPackageInstalled("npm version")
                    def minikubeStatus = sh(script: "minikube status", returnStdout: true).trim()
                    minikubeStatus.contains("host: Stopped") ? sh "minikube start" : echo "Minikube already started"
                }
            }
        }
        stage('Clean Previous Install') {
            when {
                expression {
                    return params.CLEAN_ALL
                }
            }
            steps {
                script {
                    sh("helm version")
                    def result = sh(script: 'helm list -q | wc -l', returnStdout: true).trim()
                    (result.toInteger() > 0) ? sh 'helm list -q | xargs -n 1 helm delete' : echo 'No Helm releases found.'
                    echo (result.toInteger() > 0) ? 'All Helm releases have been deleted.' : 'No Helm releases found.'
                    sh 'docker images --format "{{.Repository}}:{{.Tag}}" | grep -E "usersubscription|videogameproducts|videogamestore" | xargs -r docker rmi -f'
                    echo "**** Docker Images Pruned ****"
                    def deployments = sh(script: "kubectl get deployments --all-namespaces", returnStdout: true).trim()
                    !deployments.contains("No resources found") ? sh "kubectl delete deployments --all --all-namespaces" : echo "No deployments found"
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
                        string(name: "IMAGE_VERSION", value: "${params.IMAGE_TAG}")
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
                    echo "*** Pipeline Automation Test is in Running. This Pipeline will continue after finished. ****"
                    build(job: "videogame-store-automation-test-complete", parameters: [
                        booleanParam(name: "USERSUBSCRIPTION_TEST", value: true),
                        booleanParam(name: "VIDEOGAMEPRODUCTS_TEST", value: true),
                        booleanParam(name: "VIDEOGAMESTORE_TEST", value: true)
                    ], wait: true)                    
                }
            }
        }
        stage('Pause on Deploy') {
            steps {
                script {
                    def userInput = input(id: 'confirm', message: 'Proceed with deploy?', parameters: [ [$class: 'BooleanParameterDefinition', defaultValue: false, description: 'Click yes to proceed', name: 'Yes']])
                    DEPLOY_EKS = (userInput == 'Yes') ? true : false
                    echo (DEPLOY_EKS) ? "**** You Selected Yes. Deploy will start in a minute ****" : '**** You selected No. Deploy will abort. ****'
                }
            }
        }
        stage('Deploy on EKS') {
            when {
                expression {
                    return DEPLOY_EKS
                }
            }
            steps {
                script {
                    sleep 60
                    echo "*** FAKE DEPLOY : Deployed in EKS ****"
                }
            }
        }
    }
    post {
        success {
            script {
                echo "Pipeline Success"
                params.CLEAN_ALL ? echo "Cleaned All Helm Releases and Docker Images" : echo "Helm Releases and Docker Images are not cleaned."
                sh("docker image ls | grep usersubscription")
                sh("docker image ls | grep videogameproducts")
                sh("docker image ls | grep videogamestore")
                sh("helm list --short")
            }
            cleanWs()
        }
        failure {
            script {
                echo "Pipeline Failure"
            }
            cleanWs()
        }
    }
}

def createHelmManifest(def microservice) {
    dir("helm-integration/${microservice}") {
        sh("helm package .")
        def package = sh(script: 'ls *.tgz', returnStdout: true).trim()
        sh("helm install ${microservice} ./${package} --set image.repository=index.docker.io/dannybatchrun/${microservice},image.tag=1.0.0")
        sh("helm get manifest ${microservice}")
    }
}

def checkPackageInstalled(def command) {
    try {
        sh(command)
        echo "${command} command executed successfully"
    } catch (Exception e) {
        error("${command} command failed or is not correctly installed.")
    }
}
