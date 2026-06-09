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
ENV SPRING_PROFILES_ACTIVE=docker
ENV APP_PORT=8080

# Diretório de trabalho da aplicação
WORKDIR /app/phycocarbon

# Cria grupo e usuário não-privilegiado
RUN addgroup -S phycogroup && adduser -S phycouser -G phycogroup

# Copia o JAR do estágio de build
COPY --from=build /build/target/*.jar app.jar

# Ajusta permissões
RUN chown -R phycouser:phycogroup /app/phycocarbon

# Executa como usuário não-root
USER phycouser

# Porta exposta
EXPOSE 8080

# Health check interno
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget -qO- http://localhost:8080/api-docs > /dev/null 2>&1 || exit 1

# Entrypoint
ENTRYPOINT ["java", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:+UseContainerSupport", \
  "-jar", "app.jar"]
