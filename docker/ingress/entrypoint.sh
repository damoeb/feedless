#!/bin/sh

envsubst '$API_SERVER_NAMES $APP_SERVER_NAMES $INTERNAL_APP_URL $INTERNAL_API_URL' < /etc/nginx/nginx-template.conf > /etc/nginx/nginx.conf
cat /etc/nginx/nginx.conf
# Run Nginx
exec nginx -g 'daemon off;'
