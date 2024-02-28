FROM maven:3.9-sapmachine-21
EXPOSE 8082
ADD /target/videogamestore.jar videogamestore.jar
ENTRYPOINT ["java", "-jar", "videogamestore.jar"]
