FROM node:16-slim as build
WORKDIR /usr/node
RUN apt-get update \
    && apt-get install git=1:2.20.1-2+deb10u3 -y --no-install-recommends
COPY package.json .
COPY prisma .
COPY yarn.lock .
ENV IGNORE_SCRIPTS=true
ENV GIT_SSL_NO_VERIFY=1
RUN yarn config set "strict-ssl" false -g \
    && yarn install --prod --frozen-lockfile --ignore-optional --non-interactive --ignore-scripts --slient

FROM node:16-slim
WORKDIR /usr/node
RUN apt-get update && \
    apt-get install -y --no-install-recommends libssl1.1 && \
    rm -rf /var/lib/apt/lists/*
COPY --from=build /usr/node/node_modules/ ./node_modules
COPY node_modules/@generated node_modules/@generated
COPY node_modules/.prisma node_modules/.prisma
COPY ["dist", "docker-entrypoint.sh", "./"]
COPY prisma prisma
COPY build/app ./public
RUN mkdir plugins && \
    mkdir mounts && \
    chmod -R g-w . && \
    chmod -R o= . && \
    chgrp -R node . && \
    chown -R node node_modules/@prisma
USER node

ENTRYPOINT docker-entrypoint.sh
