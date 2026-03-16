FROM eclipse-temurin:21-jre
COPY target/Backend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
LABEL authors="irfankhansajid"
