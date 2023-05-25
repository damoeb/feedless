ARG APP_APP_VERSION
ARG APP_AGENT_VERSION
ARG APP_CORE_VERSION
FROM damoeb/feedless:app-$APP_APP_VERSION as app

FROM damoeb/feedless:agent-$APP_AGENT_VERSION as agent

FROM damoeb/feedless:core-$APP_CORE_VERSION as allinone
COPY --from=app /usr/share/nginx/html/ ./public/
COPY --from=agent /usr/node/agent ./agent/
COPY docker-aio-entrypoint.sh .

FROM damoeb/feedless:core-$APP_CORE_VERSION
USER root
ENV NODE_ENV=prod \
    APP_MAX_WORKERS=5 \
    APP_MIN_REQ_TIMEOUT_MILLIS=10000 \
    APP_MAX_REQ_TIMEOUT_MILLIS=15000 \
    # core
    APP_AUTHENTICATION=root \
    APP_JWT_SECRET=password \
    APP_API_GATEWAY_URL=http://localhost:8080 \
    APP_HOST_URL=http://localhost:8080 \
    APP_ACTUATOR_PASSWORD=password \
    APP_TIMEZONE=Europe/Berlin \
    APP_LOG_LEVEL=info \
    APP_ACTIVE_PROFILES="static" \
    APP_WHITELISTED_HOSTS="" \
    AUTH_TOKEN_ANONYMOUS_VALIDFORDAYS=3 \
    APP_ROOT_EMAIL=admin@localhost \
    APP_ROOT_SECRET_KEY=password \
    # agent
    APP_EMAIL=admin@localhost \
    APP_SECRET_KEY=password \
    APP_SECURE=false \
    APP_HOST=localhost:8080
#    DATABASE_URL=jdbc:postgresql://postgres:5432/${POSTGRES_DB}

RUN apk update \
    && apk add --no-cache nodejs npm #chromium

USER java
COPY --from=allinone /usr/feedless/ ./
CMD ["sh", "./docker-aio-entrypoint.sh"]
