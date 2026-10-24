# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy the backend build descriptor first to improve Docker layer caching.
COPY equations-api/pom.xml ./pom.xml
RUN mvn -B dependency:go-offline

COPY equations-api/src ./src
RUN mvn -B clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8081
ENTRYPOINT ["sh", "-c", "exec java -Dserver.port=${PORT:-8081} -jar app.jar"]