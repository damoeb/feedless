# Plugins 

You can extend the functionality by defining Event Hooks. There are three kind of hooks Webhook, Root-Script and User-script. Any plugin listens to at least one
[event](Events.md). Plugins are organized in the eventHook table.

# Webhooks
What is the Payload?
```json
{
  "type": "url",
  "script_or_url": "{a-webhook-url}"
}
```

# Custom Scripts
Runs in v8-sandbox
```json
{
  "type": "script",
  "ownerId": "{a-user}",
  "script_or_url": "{js-code}",
  "script_source_url": "{optional, source script from a git repository}"
}
```

# Root Scripts
`Root Scripts` are executable scripts, that reside in the `plugins` folder. 

```json
{
  "type": "rootScript",
  "ownerId": "system",
  "script_or_url": "{a-file-in-plugins-folder}"
}
```


