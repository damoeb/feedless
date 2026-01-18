
FROM node:20-alpine

WORKDIR /app
ARG APP_VERSION
ARG APP_GIT_COMMIT
ENV APP_VERSION=$APP_VERSION \
    APP_GIT_COMMIT=$APP_GIT_COMMIT

ENV NODE_ENV=production
ENV PORT=4000

# Copy only what we need from the builder
COPY --from=builder /app/dist/apps/upcoming ./dist
COPY package.json package-lock.json ./

# Install only production dependencies
RUN npm ci --omit=dev

EXPOSE 4000

CMD ["node", "dist/server/main.js"]
