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
    && yarn install --prod --frozen-lockfile --ignore-optional --non-interactive --ignore-scripts
RUN ls -lah

FROM node:16-slim
WORKDIR /usr/node
COPY --from=build node_modules .
COPY node_modules/@generated node_modules/@generated
COPY node_modules/.prisma node_modules/.prisma
COPY dist .
COPY package.json .
COPY app-dist ./public
COPY prisma .

USER node
# todo run migrations
#ENTRYPOINT ["node", "src/main.js"]
