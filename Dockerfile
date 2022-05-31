FROM openjdk:8-alpine

COPY target/uberjar/clojure-web-initializer.jar /clojure-web-initializer/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/clojure-web-initializer/app.jar"]
