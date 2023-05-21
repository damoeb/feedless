# Self-Hosting feedless

Simplest `feedless` setup is using [docker-compose](https://docs.docker.com/compose/install/linux/). 
Configuration is achived by envoronment flags defined in `.env`.

## Preparation
* Download the respective files from the repository.
```shell
wget https://raw.githubusercontent.com/damoeb/feedless/master/.env \
  https://raw.githubusercontent.com/damoeb/feedless/master/docker-compose.selfhosting.yml
```
* Customize the `.env` file

## Single Tenant (default)
The default authentication strategy is `root` which is single tenant. You can log in using `$APP_ROOT_EMAIL` and `$APP_ROOT_SECRET_KEY`. 
Changing these value require a restart or `core`.

## Multi Tenant
For multi tenant can be enabled by picking a different [authentication strategy](./authentication.md) than root login.

## Start Containers
* Start the containers
```shell
docker-compose -f docker-compose.selfhosting.yml up -d
```

Validate the authentication strategy in the logs (switch `APP_LOG_LEVEL=info`)
```shell
feedless-core_1   | 14:59:56.181 [main] INFO  PropertyService - property authentication = authRoot
```

Wait until you see the feedless banner
```shell
feedless-core_1   |               . .
feedless-core_1   |  ,-           | |
feedless-core_1   |  |  ,-. ,-. ,-| | ,-. ,-. ,-.
feedless-core_1   |  |- |-' |-' | | | |-' `-. `-.
feedless-core_1   |  |  `-' `-' `-' ' `-' `-' `-'
feedless-core_1   | -'
feedless-core_1   | 
feedless-core_1   | feedless:core v0.1.0-e144ffe https://github.com/damoeb/feedless

```
and open UI in browser `http://localhost:4200`

