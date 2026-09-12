echo "Starting deployment"
#registry_container_id=$(docker run -d -p 5000:5000 registry)
docker tag damoeb/feedless:app-latest localhost:5000/damoeb/feedless:app-latest
docker tag damoeb/feedless:app-upcoming-latest localhost:5000/damoeb/feedless:app-upcoming-latest
docker tag damoeb/feedless:core-latest localhost:5000/damoeb/feedless:core-latest
docker tag damoeb/feedless:browserautomation-latest localhost:5000/damoeb/feedless:browserautomation-latest
docker push localhost:5000/damoeb/feedless:app-latest
docker push localhost:5000/damoeb/feedless:app-upcoming-latest
docker push localhost:5000/damoeb/feedless:core-latest
docker push localhost:5000/damoeb/feedless:browserautomation-latest

kubectl apply -f k8s/feedless/feedless-browserautomation.yaml
kubectl rollout restart deployment feedless-browserautomation

kubectl apply -f k8s/feedless/feedless-web.yaml
kubectl rollout restart deployment feedless-web

kubectl apply -f k8s/feedless/upcoming-service.yaml
kubectl rollout restart deployment upcoming-service

kubectl apply -f k8s/feedless/feedless-core.yaml
kubectl rollout restart deployment feedless-core
