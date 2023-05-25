ARG APP_AGENT_VERSION
ARG APP_CORE_VERSION
ARG APP_BUNDLE_VERSION
ARG APP_TAG_BUNDLE_WEB
FROM damoeb/feedless:agent-$APP_AGENT_VERSION as agent

FROM damoeb/feedless:core-$APP_CORE_VERSION as allinone
COPY --from=agent /usr/node/agent ./agent/
COPY docker-aio-entrypoint.sh .

FROM damoeb/feedless:${APP_TAG_BUNDLE_WEB}-${APP_BUNDLE_VERSION}
USER root
ENV NODE_ENV=prod \
    APP_MAX_WORKERS=5 \
    APP_MIN_REQ_TIMEOUT_MILLIS=10000 \
    APP_MAX_REQ_TIMEOUT_MILLIS=15000 \
    APP_EMAIL=admin@localhost \
    APP_SECRET_KEY=password \
    APP_SECURE=false \
    APP_HOST=localhost:8080

RUN apk update \
    && apk add --no-cache nodejs npm chromium

USER java
COPY --from=allinone /usr/feedless/ ./
CMD ["sh", "./docker-aio-entrypoint.sh"]
