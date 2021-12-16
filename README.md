# rich-RSS

`rich-RSS` is a work in progress. It can be used as a middleware that allows you to manage feed subscriptions. Try the [live demo](https://richrss.migor.org/). `Rich` in this context is an umbrella term for the following features.

## Features

- [Network control](docs/Network-Control.md)
- [Social Feeds](docs/Social-RSS.md) extension with Comments and Timeline
- Content enrichment with Full(-text) and Quality/Quantity stats
- Aggregation of multiple feeds
- [Throttling](docs/Throttling.md) of feed sources
- [Filtering](docs/Filtering.md)
- [Web-to-Feed](docs/Web-to-Feed.md) support using [rss-proxy](https://github.com/damoeb/rss-proxy) or custom hooks
- Wayback support to rebuild old feeds
- Feed Healing of broken xml
- [Plugins](docs/Plugins.md) / Webhooks

There are planned [premium features](roadmap.md) will only available via cloud services that require a paid subscription.


## Using docker-compose (Minimal)
If you just want to try out a minimal variant of rich-rss, use the following instructions. 

```
 docker-compose up mysql rich-rss-node rich-rss-kotlin rich-rss-app
```
Here you are not running rich-graph and its dependencies [neo4j](https://neo4j.com/) and [elasticsearch](https://www.elastic.co/elasticsearch/), hence you don't have quality scoring and recommendation of related articles.



## Using docker-compose (Recommended)

The recommended way to use rich-RSS is using [docker-compose](https://docs.docker.com/compose/)

```
 docker-compose up -d
```
Then open [localhost:8080](http://localhost:8080) in the browser. 


## From source

For local development you need java-runtime 11+ and gradle 7+ (use a package manager like [jenv](https://www.jenv.be/), [sdkman](https://sdkman.io/)). Every module can be started using `gradle start`


- Start servers
```
cd 

```

## Changelog
See [changelog](changelog.md)

## Roadmap
See [roadmap](roadmap.md)

## Contact
[via twitter](https://twitter.com/damoeb)

## Related Projects
Feed:
- [feedirss](https://www.feedirss.com/)
- [datorss](https://www.datorss.com/)
- [nitter](https://github.com/zedeus/nitter)
- [invidious](https://github.com/iv-org/invidious)
- [piped](https://github.com/TeamPiped/Piped)
- [siftrss](https://siftrss.com/)

Readability Extraction:
- [xtractor](https://github.com/mohaps/xtractor)
- [readability](https://github.com/mozilla/readability)

## License
[EUPL-1.2](https://opensource.org/licenses/EUPL-1.2)
