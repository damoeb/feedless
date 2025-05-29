echo "Starting deployment"
#registry_container_id=$(docker run -d -p 5000:5000 registry)
docker tag damoeb/feedless:app-latest localhost:5000/damoeb/feedless:app-latest
docker tag damoeb/feedless:core-latest localhost:5000/damoeb/feedless:core-latest
docker tag damoeb/feedless:agent-latest localhost:5000/damoeb/feedless:agent-latest
docker push localhost:5000/damoeb/feedless:app-latest
docker push localhost:5000/damoeb/feedless:core-latest
docker push localhost:5000/damoeb/feedless:agent-latest

kubectl apply -f ../k8s/feedless-agent.yaml
kubectl rollout restart deployment feedless-agent

kubectl apply -f ../k8s/feedless-web.yaml
kubectl rollout restart deployment feedless-web

kubectl apply -f ../k8s/feedless-core.yaml
kubectl rollout restart deployment feedless-core
