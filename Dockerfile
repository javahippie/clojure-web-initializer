FROM openjdk:8-alpine

COPY target/uberjar/htmx-test.jar /htmx-test/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/htmx-test/app.jar"]
