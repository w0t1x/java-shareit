FROM maven:3.9.6-amazoncorretto-21 AS builder
COPY src app/src
COPY pom.xml app
RUN mvn -f /app/pom.xml clean package

FROM amazoncorretto:21
COPY --from=builder app/target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]