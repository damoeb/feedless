ARG APP_VERSION
FROM damoeb/feedless:app-$APP_VERSION as app

FROM damoeb/feedless:core-$APP_VERSION
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
    APP_ROOT_SECRET_KEY=password
#    DATABASE_URL=jdbc:postgresql://postgres:5432/${POSTGRES_DB}

COPY --from=app /usr/share/nginx/html/ ./public/
