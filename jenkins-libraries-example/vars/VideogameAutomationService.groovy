def call() {
    println "VideogameAutomationService initialized"
    return this;
}

def installIntoDirectory(def path, def testType) {
    dir ("${path}/cucumber-auto/${testType}") {
        sh("npm install --save @cucumber/cucumber axios pactum")
        echo "Dependencies installed for ${testType}"
    }
}

def prepareEndpoints(def microservice) {
    switch("${microservice}") {
        case "usersubscription":
            replaceEndpoints("usersubscription","store-usersubscription-example","postrequestmonthly","8090")
            replaceEndpoints("usersubscription","store-usersubscription-example","postrequestannual","8090")
            replaceEndpoints("usersubscription","store-usersubscription-example","getrequest","8090")
        break
        case "videogameproducts":
            replaceEndpoints("videogameproducts","store-videogame-products-example","postrequest","8100")
            replaceEndpoints("videogameproducts","store-videogame-products-example","getrequest","8100")
            replaceEndpoints("videogameproducts","store-videogame-products-example","deleterequest","8100")
        break
        case "videogamestore":
            replaceEndpoints("videogamestore","store-videogamestore-final-example","synchronize","8080")
            replaceEndpoints("videogamestore","store-videogamestore-final-example","postrequest","8080")
            replaceEndpoints("videogamestore","store-videogamestore-final-example","getrequest","8080")
        break    
    }
}

def replaceEndpoints(def microservice, def path, def testType, def servicePort) {
    def apiEndpoint = sh(script: "minikube service ${microservice} --url | head -n 1", returnStdout: true).toString().trim()
    dir("${path}/cucumber-auto/${testType}/features/step_definitions") {
        def file = readFile('stepdefs.js')
        def replaced = file.replace("http://localhost:${servicePort}", apiEndpoint)
        writeFile(file: 'stepdefs.js', text: replaced)
    }
    if(microservice.equals("videogamestore")) {
        dir ("store-videogamestore-final-example/cucumber-auto/synchronize") {
            def urlSubscription = sh(script: 'minikube service usersubscription --url | head -n 1', returnStdout: true).toString().trim()
            def urlVideogame = sh(script: 'minikube service videogameproducts --url | head -n 1', returnStdout: true).toString().trim()
            sh("sed -i 's|ENDPOINT_USERSUBSCRIPTION|'\"${urlSubscription}\"'|g' features/synchronize_all.feature")
            sh("sed -i 's|ENDPOINT_VIDEOGAMEPRODUCTS|'\"${urlVideogame}\"'|g' features/synchronize_all.feature")
        }
    }
}

def installDependenciesNodeJs(def microservice) {
    switch("${microservice}") {
        case "usersubscription":
            installIntoDirectory("store-usersubscription-example","postrequestmonthly")
            installIntoDirectory("store-usersubscription-example","postrequestannual")
            installIntoDirectory("store-usersubscription-example","getrequest")
        break
        case "videogameproducts":
            installIntoDirectory("store-videogame-products-example","postrequest")
            installIntoDirectory("store-videogame-products-example","getrequest")
            installIntoDirectory("store-videogame-products-example","deleterequest")
        break
        case "videogamestore":
            installIntoDirectory("store-videogamestore-final-example","synchronize")
            installIntoDirectory("store-videogamestore-final-example","postrequest")
            installIntoDirectory("store-videogamestore-final-example","getrequest")
        break
    }
}

def runTestCucumber(def microservice, def testType) {
    def path
    switch("${microservice}") {
        case "usersubscription":
            path = "store-usersubscription-example"
        break
        case "videogameproducts":
            path = "store-videogame-products-example"
        break
        case "videogamestore":
            path = "store-videogamestore-final-example"
        break
    }
    println "*** RUNNING TEST ${microservice.toUpperCase()} : ${testType.toUpperCase()} ***"
    dir ("${path}/cucumber-auto/${testType}") {
        sh("npm test")
    }
    println "*** ${microservice.toUpperCase()} : ${testType.toUpperCase()} COMPLETED SUCCESSFULLY ***"
}
