FROM openjdk:8-alpine

COPY target/uberjar/my-project.jar /my-project/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/my-project/app.jar"]
