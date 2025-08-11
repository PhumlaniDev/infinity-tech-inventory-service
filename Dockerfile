# First stage: Build the JAR
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline

COPY src ./src
RUN ./mvnw package -DskipTests

# Second stage: Minimal runtime image
# --- Final image ---
FROM eclipse-temurin:21-jre-jammy

# Create non-root user and group
RUN groupadd -g 1001 appuser && useradd -u 1001 -g appuser -s /bin/bash -m appuser

WORKDIR /app
COPY --from=builder /app/target/inventory-service-0.0.1-SNAPSHOT.jar app.jar

# Copy wait-for-it.sh into container
COPY wait-for-it.sh wait-for-it.sh
RUN chmod +x wait-for-it.sh

# Change file permissions and ownership (optional but good)
RUN chown -R appuser:appuser /app

USER appuser

# Run wait-for-it before starting app
ENTRYPOINT ["./wait-for-it.sh", "loki:3100", "--", "java", "-jar", "app.jar"]