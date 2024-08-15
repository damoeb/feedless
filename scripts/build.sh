#!/bin/bash

git pull
LATEST_COMMIT=$(git rev-parse HEAD)
LAST_COMMIT=$(cat "$PWD"/LAST_BUILD_COMMIT)

if [ "$LATEST_COMMIT" = "$LAST_COMMIT" ]; then
  echo "No updates."
else
  ./gradlew deploySaas
  echo "$LATEST_COMMIT" > "$PWD"/LAST_BUILD_COMMIT
fi
