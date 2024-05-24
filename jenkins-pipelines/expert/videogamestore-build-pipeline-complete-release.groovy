@Library('jenkins-library-videogame-store')

import java.util.Random

def service = new VideogameService().call()

pipeline {
    agent any
    parameters {
        string(name: 'BRANCH_NAME', defaultValue: 'master', description: 'Inserisci il branch per tutte le immagini di Videogame Store')
        string(name: 'RELEASE_VERSION', defaultValue: 'latest', description: 'Inserisci il versionamento del branch di release')
    }
    stages {
        stage('Checkout Branch') {
            steps {
                script {
                    sh("git checkout ${params.BRANCH_NAME}")
                }
            }
        }
        stage('Maven Stage') {
            steps {
                script {
                    sh("mvn -v")
                    service.createJarFile("store-usersubscription-example")
                    service.createJarFile("store-videogame-products-example")
                    service.createJarFile("store-videogamestore-final-example")
                }
            }
        }
        stage('Push Release Branch') {
            steps {
                script {
                    sh("git checkout -b release/${RELEASE_VERSION}")
                    sh("git push -u origin release/${RELEASE_VERSION}")
                    sh("git branch")
                }
            }
        }
        stage('Copy JAR Artifacts') {
            steps {
                script {
                    service.copyWorkspace("store-usersubscription-example","${RELEASE_VERSION}")
                    service.copyWorkspace("store-videogame-products-example","${RELEASE_VERSION}")
                    service.copyWorkspace("store-videogamestore-final-example","${RELEASE_VERSION}")
                }
            }
        }
        stage('Store Artifacts in Jenkins') {
            steps {
                script {
                    archiveArtifacts artifacts: 'store-usersubscription-example/target/usersubscription.jar', followSymlinks: false
                    archiveArtifacts artifacts: 'store-videogame-products-example/target/videogamestore.jar', followSymlinks: false
                    archiveArtifacts artifacts: 'store-videogamestore-final-example/target/videogamestore.jar', followSymlinks: false
                }
            }
        }
    }
    post {
        success {
            echo "Pipeline Success"
            cleanWs()
        }
        failure {
            echo "Pipeline Failure"
            cleanWs()
        }
    }
}
