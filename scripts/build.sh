#!/bin/bash

DEPLOY_SCRIPT=$1
LOCK_FILE="./build.lockfile"

if [ -f "${LOCK_FILE}" ]; then
  echo "Aborting, lockfile found ${LOCK_FILE}"
  exit 1
else
  echo "Creating lockfile"
  touch ${LOCK_FILE}
fi

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
    -v "${PWD}/build-cache:/opt/feedless/build-cache" \
    -it amazoncorretto:21 cd /opt/feedless && ./gradlew --no-daemon prepare && \
#  docker run -u "$UID:$GID" --workdir /opt/feedless/packages/app-web -v "${PWD}:/opt/feedless" -it zenika/alpine-chrome:117-with-node npm run test:ci && \
  docker run -u "$UID:$GID" \
    -v "${PWD}:/opt/feedless" \
    -v "${PWD}/build-cache:/opt/feedless/build-cache" \
    -it amazoncorretto:21 cd /opt/feedless && ./gradlew --no-daemon build -x packages:app-web:test && \
  ./gradlew --no-daemon bundle -x packages:app-web:test

  BUILD_EXIT_CODE=$?
  echo "BUILD exited with ${BUILD_EXIT_CODE}"
  echo "$LATEST_COMMIT" > "$PWD"/LAST_BUILD_COMMIT

  if [ "$BUILD_EXIT_CODE" = "0" ]; then
    echo "Build was successful, triggering deploy script ${DEPLOY_SCRIPT}"
    cd $(dirname ${DEPLOY_SCRIPT}) && sh $(basename ${DEPLOY_SCRIPT})
  fi
fi

rm ${LOCK_FILE}
