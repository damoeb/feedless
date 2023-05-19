# (Self-)Hosting feedless

Easiest runtime setup is using [docker-compose](https://docs.docker.com/compose/install/linux/) and an amd processor.

## Minimal Setup (Single User)

### Download
```shell
wget https://raw.githubusercontent.com/damoeb/feedless/master/.env \
  https://raw.githubusercontent.com/damoeb/feedless/master/docker-compose.minimal.yml 
```
The file `.env` contains default credentials.

### Launch
```shell
docker-compose pull
docker-compose -f docker-compose.minimal.yml up -d
```

Agents - used for prerendering - will autoconnect and reconnect.

Probably observe the logs `docker-compose logs --tail=100 -f`

