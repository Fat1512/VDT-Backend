FROM maven:3.8.3-openjdk-17 as build

WORKDIR /app/project
COPY . .
RUN mvn install -DskipTests=true

FROM openjdk:17-jdk-alpine
COPY --from=build /app/project/target/demo-0.0.1-SNAPSHOT.jar ./deploy/demo-0.0.1-SNAPSHOT.jar

ENTRYPOINT java -jar ./deploy/demo-0.0.1-SNAPSHOT.jar