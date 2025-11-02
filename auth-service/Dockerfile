FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

COPY target/auth-service-1.0-SNAPSHOT.jar auth-service.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "auth-service.jar"]


