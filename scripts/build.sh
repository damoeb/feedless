#!/bin/bash

git pull
LATEST_COMMIT=$(git rev-parse HEAD)
LAST_COMMIT=$(cat "$PWD"/LAST_BUILD_COMMIT)

if [ "$LATEST_COMMIT" = "$LAST_COMMIT" ]; then
  echo "No updates."
else

  # -Dorg.gradle.caching.debug=true
  docker run -u "$UID:$GID" \
    --workdir /opt/feedless \
    -v "${PWD}:/opt/feedless" \
    -v "${PWD}/gradle-build-cache:/opt/feedless/build-cache" \
    -it amazoncorretto:21 cd /opt/feedless && ./gradlew --no-daemon prepare
  #docker run -u "$UID:$GID" --workdir /opt/feedless/packages/app-web -v "${PWD}:/opt/feedless" -it zenika/alpine-chrome:117-with-node npm run test:ci
  docker run -u "$UID:$GID" \
    -v "${PWD}:/opt/feedless" \
    -v "${PWD}/gradle-build-cache:/opt/feedless/build-cache" \
    -it amazoncorretto:21 cd /opt/feedless && ./gradlew --no-daemon build -x packages:app-web:test
  ./gradlew --no-daemon bundle -x packages:app-web:test

#  docker-compose stop feedless-app feedless-agent feedless-core
#  docker-compose rm -f feedless-app feedless-agent feedless-core
#  docker-compose up --detach feedless-app feedless-agent feedless-core

  echo "$LATEST_COMMIT" > "$PWD"/LAST_BUILD_COMMIT
fi
