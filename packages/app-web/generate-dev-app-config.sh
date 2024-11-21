PRODUCT=$1
OFFLINE_SUPPORT=$2
echo "width product $PRODUCT $OFFLINE_SUPPORT"
echo "{
  \"apiUrl\": \"http://localhost:8080\",
  \"product\": \"${PRODUCT}\",
  \"eventRepositoryId\": \"2ec86f35-aaa1-4f13-a585-e970e5b6dd05\",
  \"operatorName\": \"your name\",
  \"operatorAddress\": \"your address\",
  \"operatorEmail\": \"your email\",
  \"offlineSupport\": ${OFFLINE_SUPPORT}
}" > src/config.json
