# Stage 1: Runtime
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Копируем готовый .jar, который Jenkins уже собрал на предыдущем шаге
COPY user-service/user-service-impl/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]