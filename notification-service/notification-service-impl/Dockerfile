FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY notification-service/notification-service-impl/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]