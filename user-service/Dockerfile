FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

COPY target/user-service-1.0-SNAPSHOT.jar user-service.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "user-service.jar"]

