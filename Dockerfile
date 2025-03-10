FROM docker.io/library/alpine:3.21.3
RUN apk add openjdk8
WORKDIR /app
COPY target/*.jar .
CMD java -jar *.jar
EXPOSE 9999
