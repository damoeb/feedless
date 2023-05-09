# Hosting feedless

## For Non-Developers
Easiest runtime setup is using docker and an amd processor.


## For Developers

```shell
docker-compose up -d postgres

gradle packages:server-core:bootRun --args='--spring.profiles.active=sso,database,dev'
```
