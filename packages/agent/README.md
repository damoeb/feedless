# Agent

Responsible for dynamic rendering using puppeteer in a headless chrome.
All prerender requests go threw a job queue and concurrent jobs are limited by `MAX_WORKERS` (default `5`) per container.

Requests that take longer than `MAX_REQ_TIMEOUT_MILLIS` (default `150000`) will be aborted.

It connects to a server instance via a websocket using a secret key issued by a user.

# Notes on memory consumption

Headless chrome can be greedy on memory and cpu.

https://paul.kinlan.me/hosting-puppeteer-in-a-docker-container/
https://www.howtogeek.com/devops/how-to-run-puppeteer-and-headless-chrome-in-a-docker-container/
