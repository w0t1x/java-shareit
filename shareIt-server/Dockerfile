FROM eclipse-temurin:21-jre-jammy
VOLUME /tmp

COPY target/shareIt-server-0.0.1-SNAPSHOT-exec.jar app.jar

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app.jar"]
