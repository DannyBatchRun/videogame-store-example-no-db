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
# Pipeline Videogame Store Complete Infrastructure - Before Start
File Name : <strong>jenkins-pipelines/expert/videogamestore-deploy-all-complete.groovy</strong>
Required Packages to Install : <strong>Java 21, Maven, NodeJS, Helm, Minikube and Kubectl (KubernetesCli) installed.</strong>< /br>
This pipeline is able to build a complete infrastructure based first on Minikube, then it performs a Test Automation with Cucumber Automatically with Scenario Outline and in the end, you can choose to deploy all this in a Cluster GKE.<br />
If you want to test this code, you make sure that all parameters of authentication with its passwords including Cluster, matching with yours. Then otherwise, Pipeline will get a failure status.< br/>
- <strong>Pipeline Call Part</strong> : This pipeline will call all pipelines attached specified in the groovy. You must create a new pipeline that matches the same name specified in this groovy file.< br/>
- <strong>Credentials Part</strong> : You can configure it in <strong>Credentials</strong> ---> <strong>Global</strong> ---> <strong>Secret Text</strong>, with the same name specified in the code logic.<br />
- <strong>GKE Cluster Part</strong> : This pipeline manage a Deploy in Google Kubernetes Engine (GKE). You must authenticate it into your cluster in local and then replace it into the pipeline groovy.<br />
- <strong>Jenkins Libraries Integration</strong> : You can configure it on <strong>Jenkins</strong> ---> <strong>Configure System</strong> in Library Section. You must put the repository with its branch and it must respect the same folders you specified with path /vars.<br />
- <strong>Jenkins Library Edit</strong> : You should modify all libraries present on <strong>jenkins-libraries/vars</strong> folder with parameters required for authentication, the same is for the paths in local that is required to change. 
# Pipeline Videogame Store Complete Infrastructure - About
- <strong>Check Running Packages</strong> : Check if the packages in local are installed and prints the Build number with parameters inserted, and switch to minikube cluster.<br />
- <strong>Clean Previous Install</strong> : It removes previous deployment on helm, docker images and kubectl resources.<br />
- <strong>Helm Install</strong> : It creates all infrastructure resources in local through Kubernetes on Minikube.<br />
- <strong>Build and Push on Docker</strong> : It calls the pipeline <strong>videogame-store-build-complete</strong> which is building jar file through Maven, Build a Docker Image and Push in a Repository on Docker Hub.<br />
- <strong>Replace Images Deployment</strong> : It calls the pipeline <strong>videogame-store-deploy-complete</strong> which replaces the image already present on helm package already deployed for the new image you specified in the pipeline with its tag associated.<br />
- <strong>Test Automation</strong> : It goes to sleep for 5 minutes to be sure that all infrastructure in local, then it launch the pipeline <strong>videogame-store-automation-test-complete</strong>, which is able to replace all endpoints of minikube services generated and with Cucumber and Scenario Outlines, is able to test each parameter with endpoints setted for every single application.<br />
- <strong>Deploy on GKE</strong> : In this stage, Pipeline is waiting an input from the user to proceed with deploy in GKE - Google Kubernetes Engine. If yes, helm switch from minikube context to the effective cluster and then, install or upgrade the deployments based on the situation in the cluster.<br />
<br />
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

# Update 03/08/2024

<strong>- Added a Pipeline that runs a complete infrastructure of all services first on Minikube and then on a Google Kubernetes Engine (GKE)</strong>
<strong>- Modified all Libraries of Jenkins with its methods.</strong>
<strong>- Modified Logic of VideogameStore. Synchronize migrated from GetRequest to PostRequest, and now is able to receive endpoints of other two services as a variable.</strong>
