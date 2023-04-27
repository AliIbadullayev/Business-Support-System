FROM openjdk:17-slim

ARG JAR_FILE=target/*.jar

COPY $JAR_FILE /hrs.jar

ENTRYPOINT exec java -jar /hrs.jar