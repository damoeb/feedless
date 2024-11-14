PRODUCT=$1
OFFLINE_SUPPORT=$2
echo "width product $PRODUCT $OFFLINE_SUPPORT"
echo "{
  \"apiUrl\": \"http://localhost:8080\",
  \"product\": \"${PRODUCT}\",
  \"eventRepositoryId\": \"a564c307-5bb5-4b23-9cf3-ed56ca70927e\",
  \"offlineSupport\": ${OFFLINE_SUPPORT}
}" > src/config.json
