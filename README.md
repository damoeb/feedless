# rich-RSS

`rich-RSS` is a web app to create feeds of most HTMLs and to manage any ATOM/RSS or JSON feed. These feed can be manipulated and remixed. 
It uses spring boot and angular. Its under development You can see that current state of development at [feedless.org](https://feedless.org)

![](docs/screenshot.png)

## Features
- Content enrichment with Full(-text)
- [Web-to-Feed](docs/Web-to-Feed.md)
- Aggregation of multiple feeds into Buckets
- [Filtering](docs/Filtering.md)
- [Throttling](docs/Throttling.md) of feed sources
- Pre-rendering of JavaScript-based websites
- Retention Policies
- Feed Healing of broken xml


# Modules
- [server core](./packages/server-core/README.md) web to feed, feed parsing, transformation, persistance.
- [app](./packages/app/README.md) managing UI for core
- [agent](./packages/agent/README.md) 


# Getting Started
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
