# Build stage
FROM public.ecr.aws/docker/library/maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY pom.xml .
RUN mvn -B -q -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -B -DskipTests package

# Runtime stage
FROM public.ecr.aws/docker/library/eclipse-temurin:17-jre
RUN apt-get update && apt-get install -y libstdc++6
WORKDIR /app

RUN groupadd -r app && useradd -r -g app app

COPY --from=build /workspace/target/*.jar app.jar

EXPOSE 8080

USER app

ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]