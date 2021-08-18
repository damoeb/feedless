FROM openjdk:11
RUN useradd -ms /bin/bash java
USER java
WORKDIR /usr/rich-rss
COPY build/libs/app.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
