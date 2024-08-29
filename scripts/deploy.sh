echo "Starting deployment"
echo "> Stopping services"
docker-compose stop feedless-app feedless-agent feedless-core && \
echo "> Removing services" && \
docker-compose rm -f feedless-app feedless-agent feedless-core && \
echo "> Starting services" && \
docker-compose up --detach feedless-app feedless-agent feedless-core && \
docker-compose scale feedless-agent=2
echo "Done"
