echo "Starting deployment"
registry_container_id=$(docker run -d -p 5000:5000 registry)
docker tag damoeb/feedless:app-latest localhost:5000/damoeb/feedless:app-latest
docker tag damoeb/feedless:core-latest localhost:5000/damoeb/feedless:core-latest
docker tag damoeb/feedless:agent-latest localhost:5000/damoeb/feedless:agent-latest
docker push localhost:5000/damoeb/feedless:app-latest
docker push localhost:5000/damoeb/feedless:core-latest
docker push localhost:5000/damoeb/feedless:agent-latest
kubectl apply -f k8s/feedless-agent.yaml k8s/feedless-web.yaml k8s/feedless-core.yaml k8s/grafana.yaml k8s/prometheus.yaml k8s/loki.yaml k8s/plausible.yaml k8s/postgis.yaml


# wait 5min
sleep 300

# Clean up the registry container at the end
docker stop "$registry_container_id"
docker rm "$registry_container_id"
