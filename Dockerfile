# Etapa 1: compilar el JAR (no hace falta Maven instalado en la VM)
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q -DskipTests package

# Etapa 2: imagen liviana solo para ejecutar
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/organization-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
