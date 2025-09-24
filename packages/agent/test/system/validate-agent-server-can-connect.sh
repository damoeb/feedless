#!/bin/bash

set -euo pipefail

# --- Config ---
AGENT_CONTAINER_NAME="agent-test-container"
SERVER_CONTAINER_NAME="server-test-container"
MAX_WAIT=30
AGENT_HOST_PORT=3000
SERVER_HOST_PORT=8080


stop_containers() {
  if docker ps -q -f name="^/${AGENT_CONTAINER_NAME}$" | grep -q .; then
    echo "Stopping container '$AGENT_CONTAINER_NAME'..."
    docker stop "$AGENT_CONTAINER_NAME" >/dev/null
  fi
  if docker ps -q -f name="^/${SERVER_CONTAINER_NAME}$" | grep -q .; then
    echo "Stopping container '$SERVER_CONTAINER_NAME'..."
    docker stop "$SERVER_CONTAINER_NAME" >/dev/null
  fi
}

cleanup() {
  echo "Cleaning up..."
  stop_containers

  agentContainerLogFile="agent-output.log"
  serverContainerLogFile="server-output.log"

  # Only collect logs if containers exist
  if docker ps -a -q -f name="^/${SERVER_CONTAINER_NAME}$" | grep -q .; then
    docker logs $SERVER_CONTAINER_NAME > "${serverContainerLogFile}" 2>/dev/null || true
  fi
  if docker ps -a -q -f name="^/${AGENT_CONTAINER_NAME}$" | grep -q .; then
    docker logs $AGENT_CONTAINER_NAME > "${agentContainerLogFile}" 2>/dev/null || true
  fi

  if [[ -f "${serverContainerLogFile}" || -f "${agentContainerLogFile}" ]]; then
    echo "See logs for details ${serverContainerLogFile} and ${agentContainerLogFile}"
  fi
}

# Set trap to ensure cleanup runs on exit
trap cleanup EXIT

# Stop any existing containers
stop_containers

echo "Starting Server container..."
# todo start db? run without db?
docker run -d --rm --name "$SERVER_CONTAINER_NAME" -p $SERVER_HOST_PORT:8080 damoeb/feedless:server-core-latest > /dev/null


echo "Starting Agent container..."

docker run -d --rm --name "$AGENT_CONTAINER_NAME" --cap-add=SYS_ADMIN -p $AGENT_HOST_PORT:3000 -e APP_DISABLE_SOCKET_SUBSCRIPTION=true damoeb/feedless:agent-latest > /dev/null

echo "Waiting for Agent to become ready..."
start_time=$(date +%s)

while true; do
  if curl -s --connect-timeout 2 --max-time 5 "http://localhost:$AGENT_HOST_PORT/readiness" | grep -q 'true'; then
    echo "✅ Agent is ready"
    break
  fi

  current_time=$(date +%s)
  elapsed=$((current_time - start_time))

  if [ $elapsed -ge $MAX_WAIT ]; then
    echo "❌ Timeout waiting for readiness check."
    exit 1
  fi

  sleep 1
done


echo "Waiting for Agent to become live..."
start_time=$(date +%s)

while true; do
  if curl -s --connect-timeout 2 --max-time 5 "http://localhost:$AGENT_HOST_PORT/liveness" | grep -q 'true'; then
    echo "✅ Agent is live"
    break
  fi

  current_time=$(date +%s)
  elapsed=$((current_time - start_time))

  if [ $elapsed -ge $MAX_WAIT ]; then
    echo "❌ Timeout waiting for liveness check."
    exit 1
  fi

  sleep 1
done

echo "✅ All tests passed successfully!"
