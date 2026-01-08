FROM eclipse-temurin:21-jre-jammy
VOLUME /tmp

COPY shareIt-gateway/target/*.jar app.jar

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app.jar"]
