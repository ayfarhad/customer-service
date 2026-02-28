FROM eclipse-temurin:17-jdk-jammy as builder

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

COPY --from=builder /app/target/customer-service-1.0.0.jar app.jar

ENV DB_URL=jdbc:postgresql://postgres:5432/customerdb \
    DB_USERNAME=postgres \
    DB_PASSWORD=password \
    JWT_SECRET=my-very-strong-secret-key-change-me

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=10s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/v1/customers || exit 1

CMD ["java", "-XX:+UseG1GC", "-XX:MaxRAMPercentage=75.0", "-Dspring.profiles.active=prod", "-jar", "app.jar"]
