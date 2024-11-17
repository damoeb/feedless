PRODUCT=$1
OFFLINE_SUPPORT=$2
echo "width product $PRODUCT $OFFLINE_SUPPORT"
echo "{
  \"apiUrl\": \"http://localhost:8080\",
  \"product\": \"${PRODUCT}\",
  \"eventRepositoryId\": \"a0af7fa0-070f-42ff-8956-836e848ad912\",
  \"offlineSupport\": ${OFFLINE_SUPPORT}
}" > src/config.json
