# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build

WORKDIR /build

# Copia dependências primeiro (cache layer)
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Copia o código-fonte
COPY src ./src

# Gera o JAR sem rodar testes
RUN mvn clean package -DskipTests -q

# Stage 2: Runtime (sem root)
FROM eclipse-temurin:17-jre-alpine

# Variável de ambiente: profile Spring ativo
ENV SPRING_PROFILES_ACTIVE=docker
ENV APP_PORT=8080

# Diretório de trabalho da aplicação
WORKDIR /app/phycocarbon

# Cria usuário não-privilegiado
RUN addgroup -S phycogroup && adduser -S phycouser -G phycogroup

# Copia o JAR do estágio de build
COPY --from=build /build/target/*.jar app.jar

# Ajusta permissões
RUN chown -R phycouser:phycogroup /app/phycocarbon

# Executa como usuário não-root
USER phycouser

# Porta exposta
EXPOSE 8080

# Entrypoint
ENTRYPOINT ["java", "-jar", "app.jar"]
