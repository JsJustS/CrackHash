FROM gradle:9.3.1-jdk21 AS build

WORKDIR /app

COPY Worker/build.gradle.kts Worker/settings.gradle.kts ./
COPY gradle ./gradle

RUN gradle dependencies --no-daemon

COPY Worker/src ./src

RUN gradle bootJar --no-daemon

FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]