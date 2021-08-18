FROM node:14 as build
WORKDIR /usr/node
RUN apt-get install git=2.25.1 -y --no-install-recommends
COPY package.json .
COPY prisma .
COPY yarn.lock .
ENV IGNORE_SCRIPTS=true
RUN yarn install --prod --frozen-lockfile --ignore-optional --non-interactive --ignore-scripts

FROM node:14
USER node
WORKDIR /usr/node
COPY --from=build node_modules node_modules
COPY node_modules/@generated node_modules/@generated
COPY node_modules/.prisma node_modules/.prisma
COPY package.json .
COPY yarn.lock .
COPY dist .
COPY prisma .
# todo run migrations
ENTRYPOINT ["node", "src/main.js"]
