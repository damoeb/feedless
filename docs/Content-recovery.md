# Content Recovery
`Web-to-Feed` may deliver insufficient results therefore you can recover data form the referenced url.
You have two options
- Metadata (URL param re=meta)
- Fulltext (URL param re=full)

## Metadata Recovery
`Metadata Recovery` will visit the referenced page and extract [JSON-LD](http://json-ld.org/), [OpenGraph](https://ogp.me/) and the common html meta tags.

## Fulltext Recovery
`Fulltext Recovery` is a extension of `Metadata Recovery` but with readability extraction. This feature can be disabled (`ENABLE_FULLTEXT`).
