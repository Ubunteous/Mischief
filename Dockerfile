# alpine
FROM docker.io/library/alpine:3.21.3
RUN apk add openjdk17

# ubuntu
# FROM docker.io/library/ubuntu:latest
# RUN apt-get update && apt-get install -y openjdk-17-jdk

WORKDIR /app

# Copy only the target jar over
COPY target/*.jar .
COPY .env .

# Run the JAR
CMD java -jar *.jar

# Open the port
EXPOSE 9999
