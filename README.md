# Feedless

Time is precious, let&#39;s automate the web, build our custom well-behaving deterministic bots. _feedless_ is a middleware/web-app to
build web-based workflows. Extendable using plugins.

## Features

* create feed from website
* merge multiple feeds
* use feed and filter title not includes &#39;Ad&#39;
* track pixel page changes of [url], but ship latest text and latest image
* track text page changes of [url], but ship diff to first for 2 weeks
* track price of product on [url] by extracting field, but shipping product fragment as pixel and markup
* use existing feed -&gt; readability, inline images and untrack urls
* generate feed, fix title by removing prefix, trim after length 20
* inbox: select feeds, filter last 24h, order by quality, pick best 12
* digest: select feed, send best 10 end of week as digest via mail
* create feed activate tracking
* create just the document repository

[![Watch the video](screenshot.png)]()

## Version 3.x (Latest)

You need [docker-compose](https://docs.docker.com/compose/install/) or [podman-compose](https://docs.podman.io/en/latest/markdown/podman-compose.1.html), here is the basic setup.

# Support & Contact

- Public Mail Group feedless@googlegroups.com
- Contact feedlessapp@proton.me

## License

Feedless is released under non-competitive FSL license, that falls back to Open Source Apache 2 after two years ([FSL-1.0-Apache-2.0](https://fsl.software/)).
