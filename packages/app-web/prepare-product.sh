PRODUCT=$1
echo "width product $PRODUCT"
echo "{ \"apiUrl\": \"http://localhost:8080\",  \"forceProduct\":\"${PRODUCT}\"}" > src/config.json
