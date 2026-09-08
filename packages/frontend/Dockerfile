# Expects pre-built SSR artifacts: run `nx run upcoming:build --configuration=production` before docker build.
# Build context: packages/frontend (so dist/apps/upcoming is available).
FROM node:24-alpine

WORKDIR /app

ARG APP_VERSION
ARG APP_GIT_COMMIT
ENV APP_VERSION=${APP_VERSION} \
    APP_GIT_COMMIT=${APP_GIT_COMMIT} \
    NODE_ENV=production \
    PORT=80

# No npm install: the Angular SSR build bundles every runtime dependency into
# server/server.mjs, so the image needs nothing but the dist output and node.
COPY dist/apps/upcoming ./

EXPOSE 80

# Angular 21+ outputs ESM server bundle as server.mjs; use main.js if your build is older
CMD ["node", "server/server.mjs"]
