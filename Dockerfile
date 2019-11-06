# Build image
FROM clojure:openjdk-11-lein-2.9.1-slim-buster as build-env
ENV HOMEDIR /usr/app/userin
WORKDIR $HOMEDIR
ADD . $HOMEDIR
RUN lein do clean, uberjar

# App image
FROM openjdk:11-jdk-slim
ENV WORKDIR /opt/userin
WORKDIR $WORKDIR
COPY --from=build-env /usr/app/userin/target .
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/opt/userin/app.jar"]
