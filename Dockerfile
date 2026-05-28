# Usamos Java 21
FROM openjdk:21-jdk-slim

# Definimos dónde está el archivo compilado
ARG JAR_FILE=target/*.jar

# Copiamos el JAR al contenedor
COPY ${JAR_FILE} app.jar

# Comando para arrancar el servicio
ENTRYPOINT ["java", "-jar", "/app.jar"]