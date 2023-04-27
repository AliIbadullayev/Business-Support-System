FROM openjdk:17-slim

ARG JAR_FILE=target/*.jar

COPY $JAR_FILE /crm.jar

ENTRYPOINT exec java -jar /crm.jar