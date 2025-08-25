FROM ghcr.io/graalvm/jdk:21 AS build

WORKDIR /app

# These JDK images are OracleLinux-based and include package managers
RUN microdnf install -y findutils && microdnf clean all

COPY build.gradle settings.gradle gradlew gradlew.bat ./
COPY gradle ./gradle
COPY src ./src

RUN chmod +x gradlew && ./gradlew nativeCompile --no-daemon

# ---- Runtime ----
FROM debian:bookworm-slim
WORKDIR /app
COPY --from=build /app/build/native/nativeCompile/market-data-service ./
CMD ["./market-data-service"]
