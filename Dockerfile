FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /project
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM openjdk:21-jdk-slim
WORKDIR /opt/storage
COPY --from=build /project/target/storage-0.0.1-SNAPSHOT.jar storage-0.0.1-SNAPSHOT.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/opt/storage/storage-0.0.1-SNAPSHOT.jar"]