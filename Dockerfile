FROM amazoncorretto:17.0.8-alpine3.18

WORKDIR /app

ARG JAR_FILE=finesapp/build/libs/finesapp-*.jar

EXPOSE 8080

COPY ${JAR_FILE} ./app.jar

ENTRYPOINT ["java", "-jar", "./app.jar"]
