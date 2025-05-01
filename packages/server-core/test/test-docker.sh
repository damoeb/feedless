# test DNS
GIT_HASH=$1
echo "Using gitHash $GIT_HASH"
# Use DOCKER_BIN if set, otherwise fallback to podman
CONTAINER_BIN="${DOCKER_BIN:-podman}"

# Run the container and check DNS resolution for br.de
if "$CONTAINER_BIN" run --rm damoeb/feedless:core-"${GIT_HASH}" nslookup br.de | grep -q br.de; then
    echo "DNS resolution succeeded"
else
    echo "DNS resolution failed"
    exit 1
fi

#docker run damoeb/feedless:core-arm nslookup br.de | grep br.de
