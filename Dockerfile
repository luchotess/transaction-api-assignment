FROM gradle:7-jdk11 AS build
COPY --chown=gradel:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle buildFatJar --no-daemon

FROM amazoncorretto:11
EXPOSE 8080:8080
RUN mkdir /app
COPY --from=build /home/gradle/src/build/libs/*.jar /app/transaction-api-assignment.jar
ENTRYPOINT ["java", "-jar", "/app/transaction-api-assignment.jar"]
