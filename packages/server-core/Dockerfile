FROM amazoncorretto:17-alpine-jdk
ARG workdir=/usr/rich-rss
WORKDIR $workdir

ARG CORE_VERSION
ARG GIT_HASH
ENV spring_profiles_active="prod" \
    LOG_LEVEL="error" \
    CORE_VERSION=$CORE_VERSION \
    GIT_HASH=$GIT_HASH

EXPOSE 8080
RUN addgroup -g 1000 java && \
    adduser -u 1000 -G java -s /bin/sh -D java
COPY --chown=java:java docker-entrypoint.sh pre-boot.sh ./
COPY --chown=java:java build/libs/app.jar app.jar

USER java
# use supervisord https://gdevillele.github.io/engine/admin/using_supervisord/
CMD ["sh", "./docker-entrypoint.sh"]
