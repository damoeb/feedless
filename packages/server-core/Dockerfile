FROM amazoncorretto:17-alpine-jdk as build
WORKDIR /usr/feedless
COPY docker-entrypoint.sh pre-boot.sh ./
COPY build/libs/app.jar app.jar


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
    && ln -sf python3 /usr/bin/python \
    && wget https://github.com/yt-dlp/yt-dlp/releases/download/2023.03.04/yt-dlp \
    && chmod +x yt-dlp && chown java:java yt-dlp && ln -s `pwd`/yt-dlp /usr/local/bin

USER java
COPY --from=build \
  --chown=java:java /usr/feedless/* ./

# use supervisord https://gdevillele.github.io/engine/admin/using_supervisord/
CMD ["sh", "./docker-entrypoint.sh"]
