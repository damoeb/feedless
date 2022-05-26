FROM openjdk:12-alpine
WORKDIR /usr/rich-rss

RUN addgroup -g 1000 java \
    && adduser -u 1000 -G java -s /bin/sh -D java

ENV spring_profiles_active=stateless
EXPOSE 8080
COPY docker-entrypoint.sh .
COPY build/libs/app.jar app.jar
RUN mkdir static && chown -R java . && ls -lah
USER java
CMD ["sh", "./docker-entrypoint.sh"]
