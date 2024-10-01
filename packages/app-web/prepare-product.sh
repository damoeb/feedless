PRODUCT=$1
OFFLINE_SUPPORT=$2
echo "width product $PRODUCT $OFFLINE_SUPPORT"
echo "{
  \"apiUrl\": \"http://localhost:8080\",
  \"upcoming\": {
    \"eventRepositoryId\": \"123\"
  },
  \"offlineSupport\": ${OFFLINE_SUPPORT},
  \"products\": [
    {
      \"hostname\": \"localhost\",
      \"product\": \"${PRODUCT}\"
    }
  ]
}" > src/config.json
