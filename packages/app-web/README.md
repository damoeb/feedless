# Web

You can run different UIs.

## Run feedless

```
yarn start:feedless
```

## Run RSS-proxy

```
yarn start:rss-proxy
```

## Tests

Test need a chromium browser, that needs to be specified via env variable
`CHROMIUM_BIN=[brave-browser]`

```
yarn test  # with browser
yarn test:ci  # headless
```
