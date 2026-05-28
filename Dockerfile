# Usamos Java 21
FROM eclipse-temurin:21-jdk-alpine

# Definimos dónde está el archivo compilado
ARG JAR_FILE=target/*.jar

# Copiamos el JAR al contenedor
COPY ${JAR_FILE} app.jar

# Comando para arrancar el servicio
ENTRYPOINT ["java", "-jar", "/app.jar"]