# =========================================================================
# Imagem do BACKEND (Spring Boot)
# Etapa 1 compila com o JDK e o Maven; etapa 2 corre só o .jar com o JRE.
# =========================================================================

# ---------- Etapa 1: compilar ----------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# 1.º só os ficheiros do Maven, para descarregar as dependências
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw && ./mvnw -B -q dependency:go-offline

# 2.º o código, e compilar (os testes correm fora do Docker)
COPY src src
RUN ./mvnw -B -q package -DskipTests

# ---------- Etapa 2: correr ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

RUN useradd --system --create-home app
COPY --from=build /app/target/*.jar app.jar
USER app

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]