# Stage 1: The "Workshop" (JDK)
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copy the wrapper and pom first to cache dependencies
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw
# This command pre-downloads your libraries so 
# future builds are much faster (only runs if pom.xml changes)
RUN ./mvnw dependency:go-offline

# Copy your source code and build the JAR
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Stage 2: The "Shipping Container" (JRE)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# We only grab the finished JAR from the "build" stage
COPY --from=build /app/target/Backend-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]