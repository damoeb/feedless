# feedless

`feedless` is an experimental feed middleware to create RSS/ATOM/Json feeds of most HTMLs or feeds and manipulate them. It's goal is to keep the web open and accessible and to create shareable data streams.

You may [Self-host](./docs/self-hosting.md) or use [feedless.org](https://feedless.org) to create and share feeds. For trivial web-to-feed use cases, [rss-proxy](https://github.com/damoeb/rss-proxy) might be sufficient.


[![Watch the video](docs/screenshot.png)](https://www.youtube.com/watch?v=PolMYwBVmzc)

## Features
- Content enrichment with Full(-text)
- Media detection using [yt-dlp](https://github.com/yt-dlp)
- [Web-to-Feed](docs/web-to-feed.md)
- [Web-to-Fragment-Feed](docs/web-to-fragment-feed.md)
- Aggregation of multiple feeds into Buckets
- [Filters](docs/filters.md)
- [JavaScript Support](./packages/agent/README.md) of JavaScript-based websites
- Inline Images for archive/privacy purposes
- Extendable using [Plugins](docs/plugins.md)
- Simple [Self-hosting](./docs/self-hosting.md)
- [Reader Mode](./docs/reader-mode.md)
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

## Links
- [RFC 4287 - The Atom Syndication Format](./docs/rfcs/RFC%204287%20The%20Atom%20Syndication%20Format.html)
- [RFC 5005 - Feed Pagination and Arching](./docs/rfcs/RFC%205005%20Feed%20Paging%20and%20Archiving.html)
- [RFC 3275 - XML-Signature Syntax and Processing](./docs/rfcs/RFC%203275_%20(Extensible%20Markup%20Language)%20XML-Signature%20Syntax%20and%20Processing.html)
- [Pingback Protocol](./docs/rfcs/Pingback%201.0.html)

## License
[EUPL-1.2](https://opensource.org/licenses/EUPL-1.2)
