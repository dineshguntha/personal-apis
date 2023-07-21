# syntax=docker/dockerfile:1

FROM openjdk:19-jdk-alpine

ARG JAR_FILE=target/*.jar
ADD ${JAR_FILE} docuflow-amdin-apis.v1.jar
EXPOSE 30001
ENTRYPOINT ["java", "-jar", "/docuflow-amdin-apis.v1.jar"]