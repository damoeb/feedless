PRODUCT=$1
OFFLINE_SUPPORT=$2
echo "width product $PRODUCT $OFFLINE_SUPPORT"
echo "{
  \"apiUrl\": \"http://localhost:8080\",
  \"product\": \"${PRODUCT}\",
  \"eventRepositoryId\": \"a13a0b54-c5dd-41ff-a023-bc9608b7f05e\",
  \"offlineSupport\": ${OFFLINE_SUPPORT}
}" > src/config.json
