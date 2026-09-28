# Faturação

Sistema de gestão de faturação e despesas: API REST em Spring Boot com PostgreSQL.


## Stack
- Java 21, Spring Boot 4 (Spring Web, Spring Data JPA, Validation)
- PostgreSQL 17 em Docker, migrações com Flyway
- Em desenvolvimento: Spring Security (JWT), frontend Angular, testes JUnit

## Correr localmente
Requisitos: JDK 21 e Docker.

    docker compose up -d
    ./mvnw spring-boot:run

A API fica em `http://localhost:8080`. A coleção do Postman está em `postman/`.

## Documentação
- [Arquitetura](docs/ARQUITETURA.md)