# ─── Stage 1: Build ───────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copia os wrappers primeiro para aproveitar o cache de camadas do Docker
COPY gradle/ gradle/
COPY gradlew settings.gradle.kts build.gradle.kts ./

# Baixa as dependências (cache se build.gradle não mudar)
RUN ./gradlew dependencies --no-daemon || true

# Copia o código-fonte e compila
COPY src/ src/
RUN ./gradlew bootJar --no-daemon -x test

# ─── Stage 2: Runtime (imagem mínima) ─────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copia apenas o JAR final
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
