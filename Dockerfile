FROM gradle:8.6-jdk21-alpine AS build

WORKDIR /app

COPY --chown=gradle:gradle . .

RUN gradle build downloadDependencies --no-daemon

FROM eclipse-temurin:21-jre-alpine AS run

WORKDIR /app

# Copy the libraries to the container
COPY --from=build /app/build/libraries/* ./

# Copy the built application to the container
COPY --from=build /app/build/libs/snoty-backend-*.jar .

ENTRYPOINT [ "java", "-cp", "*", "me.snoty.backend.ApplicationKt" ]
