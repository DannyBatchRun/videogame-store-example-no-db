@Library('jenkins-library-videogame-store') _

import com.libraries.jenkins.service.VideogameService

pipeline {
    agent any

    stages {
        stage('Hello') {
            steps {
                VideogameService.helloTest()
            }
        }
    }
}
