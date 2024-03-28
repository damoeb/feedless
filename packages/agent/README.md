# Agent

`Agents` connect to a server (env.HOST) and establish a websocket connection (graphql subscription) in order to wait for jobs. 
Those jobs are usually rendering jobs in a chromium, with optional pre and post processings.

Agents authenticate using a personalized token. They don't need a public IP, so in theory you could have them running on your local machine.
