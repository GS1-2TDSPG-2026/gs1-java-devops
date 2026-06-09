# Stage 1 — Build
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build

WORKDIR /build

# Copia o pom.xml separado para aproveitar cache de dependências
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Copia o código-fonte
COPY src ./src

# Gera o JAR (sem testes — testes rodam no CI)
RUN mvn clean package -DskipTests -q
# Stage 2 — Runtime (imagem mínima, sem root)
FROM eclipse-temurin:17-jre-alpine

# Variáveis de ambiente padrão
ENV APP_PORT=8080

WORKDIR /app/phycocarbon

RUN addgroup -S phycogroup && adduser -S phycouser -G phycogroup

COPY --from=build /build/target/*.jar app.jar

RUN chown -R phycouser:phycogroup /app/phycocarbon

USER phycouser

# ✅ Render define $PORT dinamicamente — não fixe 8080
EXPOSE $PORT

# ✅ Health check usando $PORT
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget -qO- http://localhost:${PORT:-8080}/api-docs > /dev/null 2>&1 || exit 1

# ✅ Passa a porta para o Spring Boot via argumento
ENTRYPOINT ["sh", "-c", "java \
  -XX:MaxRAMPercentage=75.0 \
  -XX:+UseContainerSupport \
  -Dserver.port=${PORT:-8080} \
  -jar app.jar"]
