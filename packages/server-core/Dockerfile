FROM openjdk:12-alpine
WORKDIR /usr/rich-rss

RUN addgroup -g 1000 java \
    && adduser -u 1000 -G java -s /bin/sh -D java

USER java
COPY build/libs/app.jar app.jar
CMD ["java",
  # see https://www.atamanroman.dev/articles/usecontainersupport-to-the-rescue/
  "-XX:+UseContainerSupport",
  "-XX:MaxRAMPercentage=75.0",
  "-XX:+UnlockExperimentalVMOptions",
  "-XX:+UseCGroupMemoryLimitForHeap",
  "-jar",
  "app.jar",
  "-Duser.timezone=${APP_TIMEZONE}",
  "--spring.profiles.active=prod"]
