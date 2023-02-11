# rich-RSS

`rich-RSS` is a web app to create feeds of most HTMLs and to manage any ATOM/RSS or JSON feed. These feed can be managed, manipulated and remixed. 
It uses spring boot and angular. NOTE: Its a work in progress!

![](docs/screenshot.png)

It is used by [rss-proxy](https://github.com/damoeb/rss-proxy).

## Features
- [Network control](docs/Network-Control.md)
- Content enrichment with Full(-text) and Quality/Quantity stats
- Aggregation of multiple feeds
- [Web-to-Feed](docs/Web-to-Feed.md)
- Prerendering of JavaScript-based websites
- Article Retention
- [Throttling](docs/Throttling.md) of feed sources
- [Filtering](docs/Filtering.md)
- [Content Recovery](docs/Content-recovery.md)
- Feed Healing of broken xml



# Modules
- [server core](./packages/server-core/README.md) web to feed, feed parsing, transformation, persistance.
- [server ui](./packages/app/README.md) managing UI for core
- [server puppeteer](./packages/server-puppeteer/README.md) 


```shell
gradle bootRun

```

## Changelog
See [changelog](changelog.md)

## Roadmap
See [roadmap](roadmap.md)

## Contact
[via twitter](https://twitter.com/damoeb)

## Related Projects
- [feedirss](https://www.feedirss.com/)
- [datorss](https://www.datorss.com/)
- [nitter](https://github.com/zedeus/nitter)
- [invidious](https://github.com/iv-org/invidious)
- [piped](https://github.com/TeamPiped/Piped)
- [siftrss](https://siftrss.com/)
- [xtractor](https://github.com/mohaps/xtractor)
- [readability](https://github.com/mozilla/readability)

## License
[EUPL-1.2](https://opensource.org/licenses/EUPL-1.2)
