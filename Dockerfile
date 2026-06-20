# =============================================================================
# Etapa 1 — Build: compila el proyecto y empaqueta el JAR ejecutable
# =============================================================================
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Cachea dependencias: copia el POM y descarga antes del código fuente
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Compila y empaqueta. -Dmaven.test.skip=true omite compilación y ejecución de
# tests: estos corren en CI, no al construir la imagen de producción.
COPY src ./src
RUN mvn -B clean package -Dmaven.test.skip=true

# =============================================================================
# Etapa 2 — Runtime: imagen ligera solo con el JRE y el JAR
# =============================================================================
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

# Usuario no-root por seguridad
RUN groupadd --system spring && useradd --system --gid spring spring
USER spring:spring

COPY --from=build /app/target/*.jar app.jar

# Railway inyecta PORT; la app lo lee vía server.port=${PORT:8080}
EXPOSE 8080

# JAVA_TOOL_OPTIONS es leído automáticamente por la JVM (p.ej. límites de memoria
# en contenedor). Railway lo puede definir como variable de entorno.
ENTRYPOINT ["java", "-jar", "app.jar"]
