FROM ghcr.io/graalvm/native-image-community:21 AS build

WORKDIR /app

RUN microdnf install -y gcc zlib-devel findutils && \
    microdnf clean all

COPY build.gradle settings.gradle gradlew gradlew.bat ./
COPY gradle ./gradle
COPY src ./src

RUN chmod +x gradlew && ./gradlew nativeCompile --no-daemon

# ---- Runtime ----
FROM debian:bookworm-slim
WORKDIR /app
COPY --from=build /app/build/native/nativeCompile/market-data-service ./
CMD ["./market-data-service"]