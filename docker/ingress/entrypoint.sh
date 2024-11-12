#!/bin/sh

envsubst < /etc/nginx/nginx-template.conf > /etc/nginx/nginx.conf

# Run Nginx
exec nginx -g 'daemon off;'
