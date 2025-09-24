#!/bin/bash

set -euo pipefail

# --- Config ---
CONTAINER_NAME="agent-test-container"
MAX_WAIT=30
HOST_PORT=3000

if docker ps -q -f name="^/${CONTAINER_NAME}$" | grep -q .; then
  echo "Container '$CONTAINER_NAME' is already running. Stopping..."
  docker stop "$CONTAINER_NAME"
fi

echo "Starting Docker container..."

docker run -d --rm --name "$CONTAINER_NAME" --cap-add=SYS_ADMIN -p $HOST_PORT:3000 -e APP_DISABLE_SOCKET_SUBSCRIPTION=true damoeb/feedless:agent-latest > /dev/null

echo "Waiting for service to become ready..."
start_time=$(date +%s)

while true; do
  if curl -s --connect-timeout 2 --max-time 5 "http://localhost:$HOST_PORT/readiness" | grep -q 'true'; then
    echo "✅ Container is ready"
    break
  fi

  current_time=$(date +%s)
  elapsed=$((current_time - start_time))

  if [ $elapsed -ge $MAX_WAIT ]; then
    echo "❌ Timeout waiting for readiness."
    docker stop "$CONTAINER_NAME" >/dev/null
    exit 1
  fi

  sleep 1
done

sleep 4

echo "Calling puppeteer endpoint..."
response=$(curl --connect-timeout 2 --max-time 5 -s "http://localhost:$HOST_PORT/puppeteer/render?url=http%3A%2F%2Fexample.org")

expected='"ok":true}'
containerLogFile="output.log"
if [[ $response == *"$expected" ]]; then
  echo "✅ Test endpoint returned expected response."
else
  echo "❌ Unexpected response from test endpoint:"
  echo "$response"
  docker logs $CONTAINER_NAME > "${containerLogFile}"
  echo "See logs for details ${containerLogFile}"
  docker stop "$CONTAINER_NAME" >/dev/null
  exit 1
fi

echo "Stopping container..."
docker stop "$CONTAINER_NAME" >/dev/null

echo "✅ Test completed successfully."
