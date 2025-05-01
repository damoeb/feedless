echo "Starting deployment"
docker run -d -p 5000:5000 registr
docker push localhost:5000/damoeb/feedless:app-latest
docker push localhost:5000/damoeb/feedless:core-latest
docker push localhost:5000/damoeb/feedless:agent-latest
kubectl apply -f k8s
