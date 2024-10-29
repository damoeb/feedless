PRODUCT=$1
OFFLINE_SUPPORT=$2
echo "width product $PRODUCT $OFFLINE_SUPPORT"
echo "{
  \"apiUrl\": \"http://localhost:8080\",
  \"upcoming\": {
    \"eventRepositoryId\": \"ee5e2fd7-4b3e-4bf0-bf74-1224b5d667ff\"
  },
  \"offlineSupport\": ${OFFLINE_SUPPORT},
  \"products\": [
    {
      \"hostname\": \"localhost\",
      \"product\": \"${PRODUCT}\"
    }
  ]
}" > src/config.json
