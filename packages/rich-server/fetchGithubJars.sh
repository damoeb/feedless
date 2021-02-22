#!/usr/bin/env bash

PERTWEE_JAR="./libs/pertwee-1.1.0.jar"
if test -f ${PERTWEE_JAR};then
    echo "$PERTWEE_JAR exists"
else
  echo "$PERTWEE_JAR does not exist"
  wget -q https://github.com/devilgate/pertwee/releases/download/v1.1.0/pertwee-1.1.0.zip
  unzip pertwee-1.1.0.zip -d ./libs/
fi
