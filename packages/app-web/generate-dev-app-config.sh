PRODUCT=$1
OFFLINE_SUPPORT=$2
echo "width product $PRODUCT $OFFLINE_SUPPORT"
echo "{
  \"apiUrl\": \"http://localhost:8080\",
  \"product\": \"${PRODUCT}\",
  \"eventRepositoryId\": \"f7618b29-ac30-4b5f-baf2-ee68d581fa90\",
  \"operatorName\": \"your name\",
  \"operatorAddress\": \"your address\",
  \"operatorEmail\": \"your email\",
  \"offlineSupport\": ${OFFLINE_SUPPORT}
}" > src/config.json
