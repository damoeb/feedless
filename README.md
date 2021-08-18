# rich-RSS

`rich-RSS` is a middleware that allows you to manage feed subscriptions as a feed consumer and feed producer. This includes archive, filter and transform RSS/ATOM or JSON feeds into a verbose rich feed. Try the [live demo](https://richrss.migor.org/). `Rich` in this context is an umbrella term for the following features.

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

There are planned [premium features](roadmap.md) will only available via cloud services that require a paid subscription.


## Using docker-compose (Minimal)
If you just want to try out a minimal variant of rich-rss, use the following instructions. 

```
 docker-compose init
 docker-compose up mysql rich-rss-node rich-rss-kotlin rich-rss-app
 
```
Here you are not running rich-graph and its dependencies [neo4j](https://neo4j.com/) and [elasticsearch](https://www.elastic.co/elasticsearch/), hence you don't have quality scoring and recommendation of related articles.



## Using docker-compose (Recommended)

The recommended way to use rich-RSS is using [docker-compose](https://docs.docker.com/compose/)

```
 docker-compose init
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

## Related
- [feedirss](https://www.feedirss.com/)
- [datorss](https://www.datorss.com/)
- [nitter](https://github.com/zedeus/nitter)
- [invidious](https://github.com/iv-org/invidious)
- [siftrss](https://siftrss.com/)

## License
[EUPL-1.2](https://opensource.org/licenses/EUPL-1.2)
