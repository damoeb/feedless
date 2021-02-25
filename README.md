# rich-RSS

`rich-RSS` is a middleware that allows you to manage feed subscriptions as a feed consumer and feed producer. This includes archive, filter and transform RSS/ATOM/OPML or JSON feeds into a verbose rich feed. Try the [live demo](https://richrss.migor.org/). `Rich` in this context is an umbrella term for the following features.

## Features

- [Network control](docs/Network-Control.md)
- [Social Feeds](docs/Social-RSS.md) extension with Comments and Timeline
- Content enrichment 
  - Full(-text) feed items
  - Quality/Quantity stats
- Aggregation of multiple feeds
- [Throttling](docs/Throttling.md) of sources  
- [Routing](docs/Routing.md)
- [Filtering](docs/Filtering.md)
- Information Overload Protection
- [Web-to-Feed](docs/Web-to-Feed.md) support using [rss-bridge](https://github.com/RSS-Bridge/rss-bridge) / [rss-proxy](https://github.com/damoeb/rss-proxy)
- Wayback support to rebuild old feeds
- [Privacy](docs/Privacy.md)

 `rich-RSS` can be configured to use the feed generators [rss-proxy](https://github.com/damoeb/rss-proxy) and [rss-bridge](https://github.com/RSS-Bridge/rss-bridge), e.g as a fallback for a broken feed.
Some features are very resource intense and require a paid subscription.

## Using docker

The simplest - tough limited - way to use rich-RSS is using [docker](https://docs.docker.com/install/).

```
 docker run -p 8080:8080 -it damoeb/rich-rss
```
Then open [localhost:8080](http://localhost:8080) in the browser. 

## Using docker-compose (Recommended)

The recommended way to use rich-RSS is using [docker-compose](https://docs.docker.com/compose/)

```
 docker-compose start
```
Then open [localhost:8080](http://localhost:8080) in the browser. 


## From source

For local development you need java-runtime 11+ and bazel 1+. Then follow these steps:


- Start server
```
cd 

```

## Changelog
See [changelog](changelog.md)

## Roadmap
See [roadmap](roadmap.md)

## Contact
[via twitter](https://twitter.com/damoeb)

## Related
- [feedirss](https://www.feedirss.com/)
- [datorss](https://www.datorss.com/)
- [nitter](https://github.com/zedeus/nitter)
- [invidious](https://github.com/iv-org/invidious)
- [siftrss](https://siftrss.com/)

## License
[GNU GPLv3](https://www.gnu.org/licenses/gpl-3.0.en.html).

