# Sources

- telegram groups
- email
- text files e.g. logs
- search engine bing.com/search?format=rss&q=khayrirrw
- with auth

We listen to people that have nothing to say.
People that talk a lot have nothing to say.
Listen to the silence, it has so much to tell you.

# Engagement

As an producer I want to monitor the behavior of other users As a consumer I want to engage with other users by
sharing/liking/commenting on articles As a consumer I want to read other people activity for an article I am interested
in As a consumer I don't want to be spammed As a consumer I want to see comments of people I don't know

It should be able to engage with other users - maybe to stay consistent with existing approaches like twitter - in the
following ways:

- comment
- like, share / retweet or archive

# Use Cases

- integrate into zettelkasten, suggest for zettelkasten
- follow my profile
- plugin support via github
  - resolve html url ->
- how does social stuff work?
- inbox/queue for your private feed
- Explore: content resolution using webhooks check https://github.com/converspace/webmention/blob/master/README.md
- comments
- search field on top to search everything

- how to sell to researchers:
  - working on paper x -> defines bucket
  - create notes

Alegorie Wolle -> Spinnen -> Weben -> Stricken Alegorie Wiese: Fremde Samen fliegen ein

- editor https://github.com/sparksuite/simplemde-markdown-editor
- kotlin: add endpoint to suggest article
- score article
- sync to github (
- archiving an article `a`:
  - `a` has to be linked to a user-article
  - `a` is quoted in a user-article
- Private Streams:
  - notifications: notification of other users
  - archive: you network
  - inbox: suggested articles that are related to an archived article

# Archive Funktion

Wenn man einen guten Artikel `a` archivieren will, soll `a` nicht nur plump in eine Liste gesteckt werden. Gemäss dem
Zettelkastenprinzip soll jeder Artikel mit mindestens einem anderen verlinkt werden mussen um einen Kontext zu schaffen.
Jeder Artikel im feed soll dann links anführen, unabhängig vom Content. Links: Artikel -> Artikel

# Done

- create bucket
- add feed to bucket
  - choose a feed from several potential feeds (native, rss-proxy, nitter)
  - see a feeds entries
- validation if feed is still working
- import/export opml
- restore created date using archive.org first harvest or correct it with1 week distance each
- readability ui

Consumer Events (Feed)

- on trigger event
- harvest feed
  - authenticate
- for each article
  - harvest article
    - authenticate
      - native
        - basic auth -> plugin
    - prerender
      - postrender actions -> plugin
  - map
    - native
      - readability
      - audio/video stream
      - main image
      - score -> plugin
  - retention policy (iff private)
  - reduce (iff private)
    - filter

Producer Events (Bucket)

- on trigger event
- filter (allocate article segment)
  - filter articles in segment
- map
  - native
    - add bucket tags -> plugin
- reduce
  - native
    - throttle
    - aggregate -> plugin
- export
  - feed

---
Feed Resolver

- Native
- Web-to-feed
- Plugins

Consumer

- Feed
  - Visibility
  - Trigger
    - On Source Change
    - On Post
    - On Mq Event
    - Scheduled
  - Pre Harvest Actions
    - Authentication
  - [X] Logs
  - Post Harvest Actions
    - [X] Readability
    - [X] Consumer Tags
    - Content Refinement
      - Content Extraction
        - Multimedia
    - Content Quality Scoring Producer
- Trigger
  - On Consumer Change
  - Scheduled
- Segment Allocation
  - By Content
  - By Time eg.g since last change
- Output
  - Map
    - Producer Tags
  - Reduce
    - Throttling
    - Aggregation
  - Format
    - Push
    - Feed
      - Authentication
      - Expose
    - Webhook


agent
- inspiration: followup on an ISBN
- social network: follow your profiles and avoid infinity pools
- data creation: new subsets of existing data, refine it
- data refinement: readability extraction, stt, ocr of videos/fotos
- search: archive of rated articles
- alert: when is the prize for coffee the lowest
- alert: router fatal error/has upgradeable firmware https://wiki.mikrotik.com/wiki/Manual:Upgrading_RouterOS
- alert: hardware manufacturer release new firmware
- archive: soundcloud likes (+digest), youtube likes backup
- inspiration: receive link -> forward to bucket

from https://www.reddit.com/r/rss/comments/ppm9hh/looking_for_an_rssapp_alternative/

- Grab major image for article
- Tag white and black list.
- You can just follow a page, even if it does not look like a collection. In theory it is a collection of different
  versions over time. There should be a field in the article to store mapping content
- like readability. A mimeType for this field should also be specified.

- import subscriptions form twitter, hn, yt
- browse UX
  - list https://www.srf.ch/audio/sounds
  - reader https://www.srf.ch/audio/sounds/nell-the-flaming-lips-where-the-viaduct-looms-ein-maerchen?id=12099281
  - feed ui like apple podcasts https://podcasts.apple.com/us/podcast/stuff-you-should-know/id278981407
      or https://philpeople.org/profiles/dominique-kuenzle
- customize feed fields opt-in/opt-out when fetching them
- yt support
- query
- retention
- feed gen pre and post flow
- list with filters and search
- reader mode
- validation
- remixing, quotes
- content creation: an article is a proxy for comments

Level of Abstraction
- manual accumulation, manual scoring: go through a lot of documents and decide if it is relevant for you
- request what you want: automated scoring, automated accumulation: search engine
- get what you want: automated querying and automated scoring, automated accumulation

Deployment Box
- rasp-pie with usb-stick storages
- app connects via super-node to this box, so you have a local backup
- you can share a secret so people can connect to your box (dyndns)
- there should be several types of secrets
  - visibility level
  - share data level
  - owner level

Next Step:
1. docker + storage mounts
3. extend the atom feed with custom attribute using the spec https://datatracker.ietf.org/doc/html/rfc4287#section-6
4. next/prev/last/first
5. pingback -> activitypub
6. feed should be signed
7. feed Ids may be spoofed
8. Landingpage: Enter a url site or create raw feed
9. display site meta (distinguish list vs article)
10. display best feeds (native > merged-native > generated). Score generated feeds based on area covered + subscribe button. Guess columns and allow filtering.


mastodon https://blog.joinmastodon.org/2018/06/how-to-implement-a-basic-activitypub-server/
sign feed https://www.w3.org/TR/2002/REC-xmldsig-core-20020212/#sec-o-Simple

tasks today:
- root note

# digest and look-ahead

if feed contain future data, the normal mechanism would list everything.
future posts should be tagged with upcoming
for events you might want to get notified in advance.
To do that the exporter would need to look ahead (look-ahead)
