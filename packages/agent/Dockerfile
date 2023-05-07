FROM zenika/alpine-chrome:with-node as build
USER root
WORKDIR /usr/node/agent
#RUN apt-get update \
#    && apt-get install git=1:2.20.1-2+deb10u3 -y --no-install-recommends
COPY package.json .
COPY yarn.lock .
COPY build/client-lib ../client-lib
RUN ls ..
#ENV GIT_SSL_NO_VERIFY=1
#RUN yarn config set "strict-ssl" false -g \
#    &&  \
RUN yarn install --prod --frozen-lockfile --non-interactive --ignore-scripts --slient

FROM zenika/alpine-chrome:with-node
WORKDIR /usr/node/agent
ENV NODE_ENV=prod \
    MAX_WORKERS=5 \
    MIN_REQ_TIMEOUT_MILLIS=10000 \
    MIN_REQ_TIMEOUT_MILLIS=15000

EXPOSE 3000
COPY --from=build \
     --chown=chrome:chrome \
     /usr/node/agent/node_modules/ ./node_modules
COPY --chown=chrome:chrome \
    ["dist", "docker-entrypoint.sh", "./"]
USER chrome

CMD ["sh", "./docker-entrypoint.sh"]
