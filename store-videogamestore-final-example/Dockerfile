FROM maven:3.8-openjdk-17
EXPOSE 8080
ADD /target/videogamestore.jar videogamestore.jar
ENTRYPOINT ["java", "-jar", "videogamestore.jar"]