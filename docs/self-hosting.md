# Self-Hosting feedless

Simplest `feedless` setup is using [docker-compose](https://docs.docker.com/compose/install/linux/).

## Preparation
Create a file to prepare the docker environment flags
```shell
mkdir feedless
cd feedless
echo 'POSTGRES_USER=postgres
POSTGRES_PASSWORD=admin
POSTGRES_DB=feedless
APP_DATABASE_URL=jdbc:postgresql://postgres-db:5432/${POSTGRES_DB}
' > feedless.env
```

Create a network, so database and feedless can comminicate
```shell
docker network create -d bridge feedless-net
```

Start postgres database
```shell
docker run --env-file=feedless.env --network=feedless-net --hostname=postgres -d postgres:15
```


## Start Feedless
The minimal setup uses an image without headless chrome, so you will not be able to render Single-Page-Applications.
```shell
docker run --env-file=feedless.env --network=feedless-net -it damoeb/feedless:aio
```

If you want JavaScript Support use the `aio-chrome` image.
```shell
docker run --env-file=feedless.env --network=feedless-net damoeb/feedless:aio-chrome
```

The all-in-one images use by default a single-tenant [authentication strategy](./authentication.md).

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
and open UI in browser [http://localhost:8080](http://localhost:8080)

