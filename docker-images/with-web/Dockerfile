ARG APP_GIT_HASH
FROM damoeb/feedless:app-$APP_GIT_HASH as app

FROM damoeb/feedless:core-$APP_GIT_HASH
ARG APP_BUILD_TIMESTAMP
ARG APP_VERSION
ENV APP_AUTHENTICATION=root \
    APP_API_GATEWAY_URL=http://localhost:8080 \
    APP_HOST_URL=http://localhost:8080 \
    APP_ACTUATOR_PASSWORD=password \
    APP_TIMEZONE=Europe/Berlin \
    APP_LOG_LEVEL=info \
    APP_ACTIVE_PROFILES="static,database" \
    APP_WHITELISTED_HOSTS="" \
    AUTH_TOKEN_ANONYMOUS_VALIDFORDAYS=3 \
    APP_ROOT_EMAIL=admin@localhost \
    APP_ROOT_SECRET_KEY=password \
    APP_ACTIVE_PROFILES="prod" \
    APP_LOG_LEVEL="error" \
    APP_BUILD_TIMESTAMP=$APP_BUILD_TIMESTAMP \
    APP_VERSION=$APP_VERSION \
    APP_GIT_HASH=$APP_GIT_HASH
#    DATABASE_URL=jdbc:postgresql://postgres:5432/${POSTGRES_DB}

COPY --from=app /usr/share/nginx/html/ ./public/
