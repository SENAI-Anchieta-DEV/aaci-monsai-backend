# Etapa 1: Build da aplicação
FROM maven:3.9-eclipse-temurin-21 AS build
# Define o encoding do sistema para UTF-8
ENV LANG=C.UTF-8
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Execução da aplicação
FROM eclipse-temurin:21-jre
WORKDIR /app
# Copia o .jar gerado na etapa anterior
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
