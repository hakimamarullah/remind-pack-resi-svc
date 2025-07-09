FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /opt/app

# Copy Maven wrapper and pom.xml (relative to project root)
COPY .mvn/ .mvn/
COPY mvnw pom.xml lombok.config ./

RUN chmod +x ./mvnw

# Copy source code
COPY src/ ./src/
RUN ./mvnw clean install -DskipTests

FROM eclipse-temurin:21 AS jre-build
WORKDIR /opt/app
COPY --from=builder /opt/app/target/*.jar app.jar

RUN jar xf app.jar
RUN ${JAVA_HOME}/bin/jdeps --ignore-missing-deps -q  \
    --recursive  \
    --multi-release 21  \
    --print-module-deps  \
    --class-path 'BOOT-INF/lib/*'  \
    app.jar > deps.info

RUN ${JAVA_HOME}/bin/jlink \
         --add-modules $(cat deps.info) \
         --strip-debug \
         --no-man-pages \
         --no-header-files \
         --compress=2 \
         --output /javaruntime

FROM debian:bookworm-slim

# Create non-root user
RUN groupadd -r spring && useradd -r -g spring spring

ENV JAVA_HOME=/opt/java/openjdk
ENV PATH="${JAVA_HOME}/bin:${PATH}"

COPY --from=jre-build /javaruntime $JAVA_HOME

# Copy jar with fixed name
COPY --from=jre-build --chown=spring:spring /opt/app/app.jar /opt/app/app.jar

# Switch to non-root user
USER spring

EXPOSE 8080

# Set default values for environment variables
ENV JAVA_OPTS="" \
    JAVA_ARGS=""

# Use exec form with sh to handle environment variables properly
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /opt/app/app.jar $JAVA_ARGS"]