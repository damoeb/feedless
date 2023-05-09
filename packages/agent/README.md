# Agent

Renders JavaScript using [puppeteer](https://pptr.dev/) in a headless chrome.

`Agents` connect to a server (env.HOST) and establish a websocket connection in order to wait for render jobs

## Getting Started

```shell
docker-compose up feedless-agent

```

# Notes on memory consumption

Headless chrome can be greedy on memory and cpu.

https://paul.kinlan.me/hosting-puppeteer-in-a-docker-container/
https://www.howtogeek.com/devops/how-to-run-puppeteer-and-headless-chrome-in-a-docker-container/
