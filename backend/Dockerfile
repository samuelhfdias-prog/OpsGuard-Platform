# ─── Stage 1: Build ───────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copia os wrappers primeiro para aproveitar o cache de camadas do Docker
COPY gradle/ gradle/
COPY gradlew settings.gradle.kts build.gradle.kts ./
RUN chmod +x gradlew

# Baixa as dependências (cache se build.gradle não mudar)
RUN ./gradlew dependencies --no-daemon

# Copia o código-fonte e compila
COPY src/ src/
RUN ./gradlew bootJar --no-daemon -x test

# ─── Stage 2: Runtime (imagem mínima) ─────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S rodojacto && adduser -S rodojacto -G rodojacto

# Copia apenas o JAR final
COPY --from=builder /app/build/libs/*.jar app.jar

USER rodojacto

EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-Djava.security.egd=file:/dev/urandom", "-jar", "app.jar"]
