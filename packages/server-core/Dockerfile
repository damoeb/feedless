# feedctl (packages/cli) is built here, in the image, for every platform we
# support, so each self-hosted instance serves the CLI build that matches its
# own API (see CliInstallScriptController). The Go sources come from the
# BuildKit named build context `cli` (`--build-context cli=../cli`, as
# buildAmdDockerImage passes it), so building this image needs no local Go.
# $BUILDPLATFORM keeps the Go build native on any host; Go cross-compiles the
# four targets itself, without emulation. The golang tag must match the
# `toolchain` line in packages/cli/go.mod.
FROM --platform=$BUILDPLATFORM golang:1.27.1-alpine AS feedctl
ENV GOTOOLCHAIN=local
WORKDIR /src
COPY --from=cli go.mod go.sum ./
RUN go mod download
COPY --from=cli cmd ./cmd
COPY --from=cli internal ./internal
ARG APP_VERSION
RUN --mount=type=cache,target=/root/.cache/go-build \
    set -eu; \
    version="${APP_VERSION:-dev}"; \
    mkdir -p /out; \
    for target in darwin/amd64 darwin/arm64 linux/amd64 linux/arm64; do \
      os="${target%/*}"; arch="${target#*/}"; \
      CGO_ENABLED=0 GOOS="$os" GOARCH="$arch" go build -trimpath \
        -ldflags "-s -w -X main.version=$version" \
        -o "/out/feedctl-$os-$arch" ./cmd/feedctl; \
    done; \
    cd /out; \
    sha256sum feedctl-darwin-amd64 feedctl-darwin-arm64 feedctl-linux-amd64 feedctl-linux-arm64 > SHA256SUMS
COPY --from=cli install.sh /out/install.sh


FROM amazoncorretto:24-alpine-jdk AS build
WORKDIR /usr/feedless
COPY docker-entrypoint.sh pre-boot.sh ./
COPY build/libs/app.jar app.jar


FROM amazoncorretto:24-alpine-jdk
ARG workdir=/usr/feedless
WORKDIR $workdir
ARG APP_VERSION
ARG APP_BUILD_TIMESTAMP
ARG APP_GIT_COMMIT

ENV APP_ACTIVE_PROFILES="prod" \
    APP_LOG_LEVEL="error" \
    APP_BUILD_TIMESTAMP=$APP_BUILD_TIMESTAMP \
    APP_VERSION=$APP_VERSION \
    APP_GIT_COMMIT=$APP_GIT_COMMIT

EXPOSE 8080
RUN apk add --no-cache curl && \
    addgroup -g 1000 java && \
    adduser -u 1000 -G java -s /bin/sh -D java && \
    mkdir debug

#RUN apk add --update --no-cache python3 \
#    && ln -sf python3 /usr/bin/python \
#    && wget https://github.com/yt-dlp/yt-dlp/releases/download/2023.03.04/yt-dlp \
#    && chmod +x yt-dlp && chown java:java yt-dlp && ln -s `pwd`/yt-dlp /usr/local/bin

RUN touch license.key && chown java:java license.key

USER java
COPY --from=build \
  --chown=java:java /usr/feedless/* ./
# An explicit destination: the glob copy above flattens directories, so it
# cannot place static/cli.
COPY --from=feedctl --chown=java:java /out/ static/cli/

# use supervisord https://gdevillele.github.io/engine/admin/using_supervisord/
CMD ["sh", "./docker-entrypoint.sh"]
