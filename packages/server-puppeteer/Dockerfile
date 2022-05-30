FROM zenika/alpine-chrome:with-node as build
USER root
WORKDIR /usr/node
#RUN apt-get update \
#    && apt-get install git=1:2.20.1-2+deb10u3 -y --no-install-recommends
COPY package.json .
COPY yarn.lock .
#ENV GIT_SSL_NO_VERIFY=1
#RUN yarn config set "strict-ssl" false -g \
#    &&  \
RUN yarn install --prod --frozen-lockfile --non-interactive --ignore-scripts --slient

FROM zenika/alpine-chrome:with-node
WORKDIR /usr/node
ENV NODE_ENV=prod
EXPOSE 3000
COPY --from=build /usr/node/node_modules/ ./node_modules
COPY ["dist", "docker-entrypoint.sh", "./"]
RUN #chmod -R g-w . && \
#    chmod -R o= . && \
#    chgrp -R node .
USER chrome

CMD ["sh", "./docker-entrypoint.sh"]
