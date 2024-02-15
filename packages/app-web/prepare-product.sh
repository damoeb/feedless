PRODUCT=$1
OFFLINE_SUPPORT=$2
echo "width product $PRODUCT $OFFLINE_SUPPORT"
echo "{ \"apiUrl\": \"http://localhost:8080\", \"forceProduct\":\"${PRODUCT}\", \"offlineSupport\":${OFFLINE_SUPPORT}}" > src/config.json
