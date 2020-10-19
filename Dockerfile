FROM gradle:jdk11-openj9

COPY . /home/app

WORKDIR /home/app

RUN gradle build --debug

ENTRYPOINT ["java","-jar","/home/app/build/libs/chat-loading-test-1.0.0.jar"]