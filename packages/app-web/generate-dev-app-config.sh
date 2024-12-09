PRODUCT=$1
OFFLINE_SUPPORT=$2
echo "width product $PRODUCT $OFFLINE_SUPPORT"
echo "{
  \"apiUrl\": \"http://localhost:8080\",
  \"product\": \"${PRODUCT}\",
  \"eventRepositoryId\": \"262758e7-1d41-4097-ba08-f5c8bc21559f\",
  \"operatorName\": \"your name\",
  \"operatorAddress\": \"your address\",
  \"operatorEmail\": \"your email\",
  \"offlineSupport\": ${OFFLINE_SUPPORT}
}" > src/config.json
