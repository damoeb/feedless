#!/bin/bash

# turn on bash's job control
set -m

cd ./agent && sh docker-entrypoint.sh &
sh ./docker-entrypoint.sh
