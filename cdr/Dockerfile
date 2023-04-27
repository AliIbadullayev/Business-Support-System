FROM openjdk:17-slim

ARG JAR_FILE=target/*.jar

COPY $JAR_FILE /cdr.jar

ENTRYPOINT exec java -jar /cdr.jar