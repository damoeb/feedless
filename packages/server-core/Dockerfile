FROM openjdk:12-alpine
WORKDIR /usr/rich-rss

RUN addgroup -g 1000 java \
    && adduser -u 1000 -G java -s /bin/sh -D java

USER java
COPY build/libs/app.jar app.jar
CMD ["java", "-jar", "app.jar", "-Duser.timezone=${APP_TIMEZONE}", "--spring.profiles.active=prod"]
