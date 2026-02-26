FROM openjdk:21-jdk-slim

# Configurar directorio de trabajo
WORKDIR /app

# Copiar JAR de la aplicación
COPY target/procesa_seniales_kafka-0.0.1-SNAPSHOT.jar app.jar

# Exponer puerto 9100
EXPOSE 9100

# Ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
