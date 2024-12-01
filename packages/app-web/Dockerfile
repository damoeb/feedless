FROM nginx:1.23.4-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY build/generated /usr/share/nginx/html/verticals-data
COPY www/browser /usr/share/nginx/html
RUN ls -l /usr/share/nginx/html | wc
RUN if [ $(ls -l /usr/share/nginx/html | wc -l) -gt 30 ]; \
    then echo "webapp files copied"; \
    else echo "webapp files are not copied"; false; \
    fi
ARG APP_VERSION
ARG APP_GIT_HASH
ENV APP_VERSION=$APP_VERSION \
    APP_GIT_HASH=$APP_GIT_HASH
