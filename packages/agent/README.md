# Agent

`Agents` connect to a server (env.HOST) and establish a websocket connection (graphql subscription) in order to wait for jobs.
Those jobs are usually rendering jobs in a chromium, with optional pre and post processings.

Agents authenticate using a personalized token. They don't need a public IP, so in theory you could have them running on your local machine.

## Getting Started

The minimal way to run an agent, you need a secret key of a user.

```
APP_SECRET_KEY=${key} APP_CHROMIUM_BIN=brave-browser yarn start
```

## Envs

| env                          | type    | required | description      | default                   |
| ---------------------------- | ------- | -------- | ---------------- | ------------------------- |
| APP_EMAIL                    | string  | no       | email of user    | admin@localhost           |
| APP_SECRET_KEY               | string  | yes      | key of user      |                           |
| APP_SECURE                   | boolean | no       | use ssl          | false                     |
| APP_HOST                     | string  | no       | feedless server  | localhost:8080            |
| APP_PRERENDER_TIMEOUT_MILLIS | integer | no       | max timeouts     | 40000                     |
| APP_CHROMIUM_BIN             | string  | no       | path to chromium | /usr/bin/chromium-browser |

Note that if you run the server with dev profile, there might be overwrites in place, so
the values are not sources by .env.
