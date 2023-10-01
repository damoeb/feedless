#!/bin/bash

cd ./agent && sleep 10 && sh docker-entrypoint.sh &
export APP_ROOT_SECRET_KEY=$APP_SECRET_KEY
sh ./docker-entrypoint.sh
