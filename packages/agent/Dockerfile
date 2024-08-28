FROM zenika/alpine-chrome:117-with-node as build
USER root
WORKDIR /usr/node/agent
#RUN apt-get update \
#    && apt-get install git=1:2.20.1-2+deb10u3 -y --no-install-recommends
COPY package.json .
COPY yarn.lock .
#COPY build/client-lib ../client-lib
#ENV GIT_SSL_NO_VERIFY=1
#RUN yarn config set "strict-ssl" false -g \
#    &&  \
RUN yarn install --prod --frozen-lockfile --non-interactive --ignore-scripts --slient --network-timeout 600000

FROM zenika/alpine-chrome:117-with-node
WORKDIR /usr/node/agent
ARG APP_VERSION
ARG APP_GIT_HASH
ENV APP_VERSION=$APP_VERSION \
    APP_GIT_HASH=$APP_GIT_HASH \
    NODE_ENV=prod \
    APP_MAX_WORKERS=5 \
    APP_MIN_REQ_TIMEOUT_MILLIS=10000 \
    APP_MAX_REQ_TIMEOUT_MILLIS=15000

EXPOSE 3000
COPY --from=build \
     --chown=chrome:chrome \
     /usr/node/agent/node_modules/ ./node_modules
COPY --chown=chrome:chrome \
    ["dist", "docker-entrypoint.sh", "./"]
USER chrome

CMD ["sh", "./docker-entrypoint.sh"]
