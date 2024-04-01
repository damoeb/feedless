#!/bin/sh

GIT_HASH=$1
MAJOR=$2
MINOR=$3
PATCH=$4

echo "GIT_HASH: $GIT_HASH"
echo "VERSION: $MAJOR.$MINOR.$PATCH"

for imageName in core agent app aio-with-web aio-chromium; do
  dockerImage="damoeb/feedless:$imageName"
  echo "$dockerImage-$GIT_HASH"
  echo "-> $dockerImage-$MAJOR.$MINOR.$PATCH"
  docker tag "$dockerImage"-"$GIT_HASH" "$dockerImage"-"$MAJOR"."$MINOR"."$PATCH"
  echo "-> $dockerImage-$MAJOR.$MINOR"
  docker tag "$dockerImage"-"$GIT_HASH" "$dockerImage"-"$MAJOR"."$MINOR"
  echo "-> $dockerImage-$MAJOR"
  docker tag "$dockerImage"-"$GIT_HASH" "$dockerImage"-"$MAJOR"
  echo "-> $dockerImage"
  docker tag "$dockerImage"-"$GIT_HASH" "$dockerImage"
done
