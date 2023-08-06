FROM maven:3.8-jdk-11
EXPOSE 8081
ADD /target/usersubscription.jar usersubscription.jar
ENTRYPOINT ["java", "-jar", "usersubscription.jar"]