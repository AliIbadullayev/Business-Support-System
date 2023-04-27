FROM openjdk:17-slim

ARG JAR_FILE=target/*.jar

COPY $JAR_FILE /brt.jar

ENTRYPOINT exec java -jar /brt.jar