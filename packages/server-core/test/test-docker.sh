# test DNS
GIT_HASH=$1
echo "Using gitHash $GIT_HASH"
docker run damoeb/feedless:core-${GIT_HASH} nslookup br.de | grep br.de
#docker run damoeb/feedless:core-arm nslookup br.de | grep br.de
