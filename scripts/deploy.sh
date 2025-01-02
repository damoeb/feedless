echo "Starting deployment"
echo "> Stopping services"
docker-compose stop feedless-ingress feedless-apps feedless-api-agent feedless-api && \
echo "> Removing services" && \
docker-compose rm -f feedless-ingress feedless-apps feedless-api-agent feedless-api && \
echo "> Starting services" && \
docker-compose up --detach feedless-ingress feedless-apps feedless-api-agent feedless-api && \
docker-compose scale feedless-agent=2 && \
docker-compose up --detach feedless-jobs-runner-agent feedless-jobs-runner && \
docker-compose scale feedless-jobs-runner-agent=2 && \

echo "Done"
