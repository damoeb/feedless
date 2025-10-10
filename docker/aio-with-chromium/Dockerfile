ARG APP_GIT_COMMIT
FROM damoeb/feedless:agent-$APP_GIT_COMMIT as agent

FROM damoeb/feedless:aio-$APP_GIT_COMMIT

USER root
ARG APP_BUILD_TIMESTAMP
ARG APP_VERSION
ENV APP_MAX_WORKERS=5 \
    APP_MIN_REQ_TIMEOUT_MILLIS=10000 \
    APP_MAX_REQ_TIMEOUT_MILLIS=15000 \
    APP_EMAIL=admin@localhost \
    APP_SECURE=false \
    NODE_ENV=prod \
    APP_HOST=localhost:8080 \
    APP_ACTIVE_PROFILES="prod" \
    APP_LOG_LEVEL="error" \
    APP_BUILD_TIMESTAMP=$APP_BUILD_TIMESTAMP \
    APP_VERSION=$APP_VERSION \
    APP_GIT_COMMIT=$APP_GIT_COMMIT

RUN apk update \
    && apk add --no-cache nodejs npm icu-data-full chromium

COPY --from=agent /usr/node/agent ./agent/
COPY docker-aio-entrypoint.sh .

USER java
CMD ["sh", "./docker-aio-entrypoint.sh"]
