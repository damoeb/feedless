FROM openjdk:19-alpine
ARG workdir=/usr/rich-rss
WORKDIR $workdir

ARG CORE_VERSION
ARG GIT_HASH
ENV spring_profiles_active="prod,stateless" \
    AUTH_TOKEN_ANONYMOUS_VALIDFORDAYS="" \
    AUTH_TOKEN_USER_VALIDFORDAYS="" \
    PUPPETEER_HOST="" \
    LOG_LEVEL="error" \
    CORE_VERSION=$CORE_VERSION \
    MASTER_URL="https://richrss.migor.org" \
    GIT_HASH=$GIT_HASH

EXPOSE 8080
COPY docker-entrypoint.sh ./
COPY build/libs/app.jar app.jar
RUN addgroup -g 1000 java && \
    adduser -u 1000 -G java -s /bin/sh -D java && \
    mkdir static && \
    echo "serve static assets <a href=\"https://docs.docker.com/engine/reference/commandline/run/#mount-volume--v---read-only\" target=\"_blank\">by mounting them</a> in $workdir/static" > static/index.html && \
    chmod -R g-w . && \
    chmod -R o= . && \
    chown -R java .
USER java
CMD ["sh", "./docker-entrypoint.sh"]
