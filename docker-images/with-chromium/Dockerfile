ARG APP_VERSION
FROM damoeb/feedless:agent-$APP_VERSION as agent

FROM damoeb/feedless:aio-$APP_VERSION

USER root
ENV APP_MAX_WORKERS=5 \
    APP_MIN_REQ_TIMEOUT_MILLIS=10000 \
    APP_MAX_REQ_TIMEOUT_MILLIS=15000 \
    APP_EMAIL=admin@localhost \
    APP_SECURE=false \
    NODE_ENV=prod \
    APP_HOST=localhost:8080

RUN apk update \
    && apk add --no-cache nodejs npm icu-data-full chromium

COPY --from=agent /usr/node/agent ./agent/
COPY docker-aio-entrypoint.sh .

USER java
CMD ["sh", "./docker-aio-entrypoint.sh"]
