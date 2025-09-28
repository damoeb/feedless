FROM node:24-alpine

WORKDIR /usr/app
COPY . .
RUN ls
RUN yarn install --frozen-lockfile --non-interactive --ignore-scripts --slient --network-timeout 600000

RUN yarn build

CMD ["node", "dist/server.js"]
