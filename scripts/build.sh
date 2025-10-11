#!/bin/bash

LOCK_FILE="./build.lockfile"

echo "Remove unused docker images"
docker rmi $(docker images -q damoeb/feedless)

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
    -it amazoncorretto:24 cd /opt/feedless && ./gradlew --no-daemon clean bundle && \

  BUILD_EXIT_CODE=$?
  echo "BUILD exited with ${BUILD_EXIT_CODE}"
  echo "$LATEST_COMMIT" > "$PWD"/LAST_BUILD_COMMIT
fi

rm ${LOCK_FILE}
