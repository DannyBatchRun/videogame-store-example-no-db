FROM maven:3.9-sapmachine-21
EXPOSE 8081
ADD /target/usersubscription.jar usersubscription.jar
ENTRYPOINT ["java", "-jar", "usersubscription.jar"]
