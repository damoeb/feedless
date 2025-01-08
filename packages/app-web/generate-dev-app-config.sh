PRODUCT=$1
OFFLINE_SUPPORT=$2
eventRepositoryId=$EVENT_REPOSITORY_ID
echo "with product $PRODUCT $OFFLINE_SUPPORT"
if [ -z "${eventRepositoryId}" ]; then
  echo 'EVENT_REPOSITORY_ID not found (please patch .env)'
  exit 1
fi
echo "{
  \"apiUrl\": \"http://localhost:8080\",
  \"product\": \"${PRODUCT}\",
  \"eventRepositoryId\": \"${eventRepositoryId}\",
  \"operatorName\": \"your name\",
  \"operatorAddress\": \"your address\",
  \"operatorEmail\": \"your email\",
  \"offlineSupport\": ${OFFLINE_SUPPORT}
}" > src/config.json
