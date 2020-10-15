FROM adoptopenjdk/openjdk11:alpine-jre
WORKDIR /home/app
COPY build/libs/chat-loading-test-1.0.0.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]