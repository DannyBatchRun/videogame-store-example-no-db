# videogame-store-example-no-db
This Project have a full infrastructure of example based on a simple automation with three microservices based on Spring API Rest.<br />
# Spring API Rest Microservices
<strong>UserSubscription</strong> ---> Add an existing client on a Monthly or Annual Subscription.<br />
<strong>VideoGameProducts</strong> ---> Add Videogames or Remove Existing Products<br />
<strong>VideoGameStore Final</strong> ---> Synchronize ArrayLists on UserSubscription and VideogameProducts and then, add videogame to each customer.<br />
<br />
You can import this project on your IDE and then test it.<br />
Is not required any MySQL Database or similar. This is a fake database based on Collection Framework<br />
# Automation Part
<strong>helm-integration</strong> ---> Every folder have a manifest helm dedicated to each microservices, and is able to deploy it on a cluster Kubernetes or in local instance (minikube)<br />
<strong>jenkins-libraries-example</strong> ---> Each pipeline expert is properly configured in a external libraries that calls some peculiar functions.<br />
<strong>jenkins-pipelines</strong> ---> <strong>basic/expert</strong> : Every pipeline is a groovy script with a High Language CI/CD based. Every folder is separated from basic language (in local), to expert (more complex)<br />
<strong>logstash-configurations</strong> ---> Logstash is configured for each microservice, and is able to communicate with Elasticsearch.<br />
<strong>cucumber</strong> ---> Each microservice have a folder named "cucumber", dedicated to its client based on Automation Test. Each parameter is configured exclusively for Jenkins, through the paramterized build that replace every single for test the endpoint.<br />
<strong>cucumber-auto</strong> ---> Each microservice have a folder named "cucumber-auto", dedicated to its client based on Automation Test. Each microservice is properly configured with some parameters of example with Scenario's Outline.<br />
Enjoy!<br />

# Update 02/08/2024

This project have now the following integrations :<br />
<br />
<strong>- Added Dependencies with Actuator, Prometheus and Logstash with its configuration files.</strong><br />
<strong>- Added Cucumber Test Automation with dependencies and configuration based on NodeJS.</strong><br />
<strong>- Removed simple Jenkins Groovy Files, replaced by another files with High-Level Language CI/CD based.</strong><br />

# Update 02/27/2024

<strong>- Added some pipelines in High-Level Language, based on Build/Deploy/Automation Test of Each Microservice.</strong><br />
<strong>- Added cucumber-auto, configured with some parameters of example in multiple Scenario's Outline.</strong><br />
<strong>- Upgrade from Java 17 to Java 21.</strong><br />
<strong>- Updated Main description of Readme.</strong><br />
