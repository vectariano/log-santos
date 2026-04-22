# Build
FROM maven:3.9.9-eclipse-temurin-21 AS builder
LABEL authors="nikitza"
WORKDIR /build
COPY . .
RUN mvn dependency:go-offline
RUN mvn clean package -DskipTests --no-transfer-progress


# Runtime
FROM eclipse-temurin:21-jre-alpine AS runtime
ARG MODULE=controller
WORKDIR /app
RUN addgroup -S spring && adduser -S spring -G spring
USER spring
COPY --from=builder /build/${MODULE}/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", \
               "-XX:+UseContainerSupport", \
               "-XX:MaxRAMPercentage=75.0", \
               "-XX:+ExitOnOutOfMemoryError", \
               "-jar", \
               "app.jar"]