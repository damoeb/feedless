FROM amazoncorretto:17-alpine-jdk
ARG workdir=/usr/feedless
WORKDIR $workdir

ARG APP_CORE_VERSION
ARG APP_GIT_HASH
ENV APP_ACTIVE_PROFILES="prod" \
    APP_LOG_LEVEL="error" \
    APP_CORE_VERSION=$APP_CORE_VERSION \
    APP_GIT_HASH=$APP_GIT_HASH
EXPOSE 8080
RUN addgroup -g 1000 java && \
    adduser -u 1000 -G java -s /bin/sh -D java

RUN apk add --update --no-cache python3 \
    && ln -sf python3 /usr/bin/python

RUN wget https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp \
    && chmod +x yt-dlp && chown java:java yt-dlp && ln -s `pwd`/yt-dlp /usr/local/bin

USER java
COPY --chown=java:java docker-entrypoint.sh pre-boot.sh ./
COPY --chown=java:java build/libs/app.jar app.jar

# use supervisord https://gdevillele.github.io/engine/admin/using_supervisord/
CMD ["sh", "./docker-entrypoint.sh"]
