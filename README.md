# Storage Service

![Tests](https://github.com/guapman/storage-service/actions/workflows/test_and_build.yml/badge.svg)

## Overview
Basic implementation of service for storing and sharing files.
Developed using Java, Spring Boot, MongoDB and MinIO.
Code is open for future improvements.

[API documentation](API.md)

### Prerequisites

- Java 17+
- Maven
- Docker
- MongoDB (for local run)
- MinIO (for local run)

### Build and Run

To start service and all its dependencies in docker:
```sh
docker-compose up --build
```
Once started, it can be accessed at http://localhost:8080/swagger-ui/index.html

For local build run:
```sh
mvnw package
java -jar ./target/storage-0.0.1-SNAPSHOT.jar
```
