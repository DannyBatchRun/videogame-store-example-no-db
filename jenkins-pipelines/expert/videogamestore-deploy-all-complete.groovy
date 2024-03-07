@Library('jenkins-library-videogame-store')

def deployService = new VideogameServiceDeploy().call()
def mainService = new VideogameServiceInfrastructure().call()
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
                    mainService.executeCommand("helm version")
                    mainService.executeCommand("java --version")
                    mainService.executeCommand("mvn -v")
                    mainService.executeCommand("npm version")
                    def minikubeStatus = sh(script: "minikube status || true", returnStdout: true).trim()
                    minikubeStatus.contains("host: Stopped") ? mainService.executeCommand("minikube start") : 'Minikube already started'
                    mainService.executeCommand("minikube version")
                    mainService.controlContext("minikube")
                }
            }
        }
        stage('Clean Previous Install') {
            steps {
                script {
                    println "**** Cleaning old builds with Helm and Docker ****"
                    mainService.cleanLocalInfrastructures()
                    println "**** Docker Images Pruned ****"
                }
            }
        }
        stage('Helm Install') {
            steps {
                script {
                    println "**** Creating Three Helm Manifests Empty. It will start in a minute. ****"
                    sleep 60
                    mainService.createHelmManifest("usersubscription")
                    mainService.createHelmManifest("videogameproducts")
                    mainService.createHelmManifest("videogamestore")
                }
            }
        }
        stage('Build and Push on Docker') {
            steps {
                script {
                    println "*** Pipeline Build in Local is in Running. This Pipeline will continue after finished. ****"
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
                    println "*** Pipeline Deploy in Local is in Running. This Pipeline will continue after finished. ****"
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
                    println "*** Pipeline Automation Test is in Running. This Pipeline will continue after finished. ***"
                    println "*** Waiting Containers for start. Sleep for 5 minutes. ***"
                    catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
                        build(job: "videogame-store-automation-test-complete", parameters: [
                            booleanParam(name: "USERSUBSCRIPTION_TEST", value: true),
                            booleanParam(name: "VIDEOGAMEPRODUCTS_TEST", value: true),
                            booleanParam(name: "VIDEOGAMESTORE_TEST", value: true)
                        ], wait: true)
                    }
                    if (currentBuild.result == 'UNSTABLE') {
                        def testInput = input(id: 'confirm', message: 'Pipeline is unable to perform test automation. Can you proceed anyway?', parameters: [ [$class: 'BooleanParameterDefinition', defaultValue: false, description: 'Click yes to proceed', name: 'Yes']])
                        if (!testInput) {
                            println "**** You selected No. Pipeline will not proceed. ****"
                            currentBuild.result = 'UNSTABLE'
                        } else {
                            println "**** You Selected Yes. Pipeline will continue. Please check if the application is running as expected. ****"
                            sleep 60
                        }
                    }
                }
            }
        }
        stage('Deploy on GKE') {
            steps {
                script {
                    def userInput = input(id: 'confirm', message: 'Proceed with GKE deploy?', parameters: [ [$class: 'BooleanParameterDefinition', defaultValue: false, description: 'Click yes to proceed', name: 'Yes']])
                    DEPLOY_GKE = userInput
                    if (DEPLOY_GKE) {
                        println "**** You Selected Yes. GKE Deploy will start in a minute ****"
                        sleep 60
                        mainService.controlContext("gke_ethereal-anthem-416313_us-central1_videogame-cluster-gke")
                        mainService.installOrUpgradeHelmManifest("usersubscription","${params.IMAGE_TAG}","8090")
                        mainService.installOrUpgradeHelmManifest("videogameproducts","${params.IMAGE_TAG}","8100")
                        mainService.installOrUpgradeHelmManifest("videogamestore","${params.IMAGE_TAG}","8080")
                    } else {
                        println "**** You selected No. Pipeline will complete without GKE deployment. ****"
                        currentBuild.result = 'SUCCESS'
                    }
                }
            }
        }
    }
    post {
        success {
            script {
                println "**** Pipeline SUCCESS ****"
                def messageSuccess = DEPLOY_GKE ? "**** Successfully Deployed on GKE Cluster ****" : "**** Not Deployed on GKE Cluster ****"
                println messageSuccess
                println "**** Docker Images ****"
                sh("docker image ls | grep usersubscription")
                sh("docker image ls | grep videogameproducts")
                sh("docker image ls | grep videogamestore")
                sh("helm list --short")
            }
            cleanWs()
        }
        failure {
            script {
                println "**** Pipeline FAILURE ****"
                def messageFailure = DEPLOY_GKE ? "**** Something went wrong during deploy on GKE Cluster ****" : "**** Oops! Something went wrong! Please retry or review your code before launching Pipeline again. ****"
                println messageFailure
            }
            cleanWs()
        }
        unstable {
            script {
                println "**** Pipeline UNSTABLE ****"
                println "**** Cucumber is not able to communicate with service endpoint. There is an issue with your instance of Minikube installed ****"
            }
        }
    }
}
