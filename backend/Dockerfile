# Build with the project's own Gradle wrapper, then run on a slim JRE.
FROM eclipse-temurin:17-jdk AS build
WORKDIR /workspace

# Dependencies first, so editing source does not download them again.
COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle gradle
RUN ./gradlew dependencies --no-daemon -q > /dev/null

COPY src src
RUN ./gradlew bootJar --no-daemon -q -x test

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/build/libs/*.jar app.jar
# Spring Boot reads ./config/application.yml on top of the packaged one,
# which is where docker-compose mounts the Stripe keys, if there are any.
RUN mkdir config
USER 1000
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
