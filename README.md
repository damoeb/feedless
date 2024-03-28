# feedless

`feedless` is an API toolbox for web scraping and web automation, the tools steer into the direction of ITTT or zapier. If you endorse this mission join [me on discord](https://discord.gg/6ySGeryD).

## Products
Due to its general nature I shaped a couple of separate `products` for a special use case. Their user interface is optimized for their particular use case, behavior in the backend is selectively 
toggled using feature flags. Custom functionality outside of `feedless`' core is implemented using a plugin architecture, a core citizen.

- [RSS feed builder](https://github.com/damoeb/rss-proxy)
- [Page change tracker](https://github.com/damoeb/visualdiff)

## Getting Started
It is not there yet. refer to the products above instead and support them. 

## Features
- Authentication: Magic Link via Email (authMail), oauth (sso), user/password
- Persistence
- Plugins Support
- Rendering in headless chrome using [agents](./packages/agent/README.md)
- Plans and feature constraints
- Product support
- Monitoring
- Caching
- Throttling
- Self Hosting Support

## Architecture
The architecture is rather simple. 

A web based user interface (app) interacts with a graphQL API offered by the server, that outsources some operations to a horde of agents.

## Client Modules
- [app](./packages/app-web/README.md) angular UI to manage feeds ([angular](angular.io/))

## Server Modules
- [core](./packages/server-core/README.md) Stateless backend ([spring boot](https://spring.io/projects/spring-boot/))
- [agent](./packages/agent/README.md) Puppeteer wrapper to interact with headless chromium ([nestjs](https://nestjs.com/))


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
[FSL-1.1-Apache-2.0](https://github.com/getsentry/fsl.software/blob/main/FSL-1.1-Apache-2.0.template.md)

This project is licenced under [FSL](https://fsl.software), which is a non-competitive license, that becomes open-source Apache 2 after two years.
