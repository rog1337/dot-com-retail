FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

COPY gradlew settings.gradle ./
COPY gradle ./gradle
COPY backend/build.gradle.kts ./backend/

RUN chmod +x gradlew
RUN ./gradlew dependencies

COPY backend/src ./backend/src
RUN ./gradlew :backend:bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

VOLUME /uploads

COPY --from=build /app/backend/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
