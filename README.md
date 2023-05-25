# feedless

`feedless` is a web app to create RSS+ feeds of most HTMLs and to manage any ATOM/RSS or JSON feed. These feed can be manipulated and remixed. 
You may [Self-host](./docs/self-hosting.md) or join [feedless.org](https://feedless.org) to create and share feeds.


[![Watch the video](docs/screenshot.png)](https://www.youtube.com/watch?v=PolMYwBVmzc)

## Features
- Content enrichment with Full(-text)
- Media detection using [yt-dlp](https://github.com/yt-dlp)
- [Web-to-Feed](docs/web-to-Feed.md)
- Aggregation of multiple feeds into Buckets
- [Filters](docs/filters.md)
- [JavaScript Support](./packages/agent/README.md) of JavaScript-based websites
- Inline Images for archive/privacy purposes
- Extendable using [Plugins](docs/plugins.md)
- Simple [Self-hosting](./docs/self-hosting.md)
- [Third-party migration](./docs/third-party-migration.md) 

# Client Modules
- [app](./packages/app-web/README.md) angular UI to manage feeds ([angular](angular.io/))
- [cli](./packages/app-cli/README.md) CLI to query articles ([node](https://nodejs.org/))

# Server Modules
- [core](./packages/server-core/README.md) Stateless backend ([spring boot](https://spring.io/projects/spring-boot/))
- [agent](./packages/agent/README.md) Puppeteer wrapper ([nestjs](https://nestjs.com/))

# Getting Started
See [Self-hosting](./docs/self-hosting.md) or [development](./docs/development.md)

## Changelog
See [changelog](changelog.md)

## Contact
feedlessapp/at/proton/dot/me

## Related Projects
- [feedirss](https://www.feedirss.com/)
- [nitter](https://github.com/zedeus/nitter)
- [invidious](https://github.com/iv-org/invidious)
- [siftrss](https://siftrss.com/)
- [xtractor](https://github.com/mohaps/xtractor)

## License
[EUPL-1.2](https://opensource.org/licenses/EUPL-1.2)
