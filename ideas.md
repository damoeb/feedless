# Other
- other interesting sources are telegram groups, email
- preprocessing steps before running rss-proxy: click here and here...

# Engagement
As an producer I want to monitor the behavior of other users
As a consumer I want to engage with other users by sharing/liking/commenting on articles
    As a consumer I want to read other people activity for an article I am interested in
As a consumer I don't want to be spammed
As a consumer I want to see comments of people I don't know


It should be able to engage with other users - maybe to stay consistent with existing approaches like twitter - in
the following ways:
- comment
- like, share / retweet or archive


# Use Cases
- integrate into zettelkasten, suggest for zettelkasten
- follow my profile
- plugin support via github
  - resolve html url ->
- query bing.com/search?format=rss&q=khayrirrw, hn
- how does social stuff work?
- inbox/queue for your private feed
- Explore: content resolution using webhooks
check https://github.com/converspace/webmention/blob/master/README.md

- use kotlin linter https://blog.mindorks.com/code-formatting-in-kotlin-using-ktlint
- how to sell to researchers:
  - working on paper x -> defines bucket
  - create notes

- feed ui like apple podcasts https://podcasts.apple.com/us/podcast/stuff-you-should-know/id278981407
    or https://philpeople.org/profiles/dominique-kuenzle
- add missing filters videoCount, audioCount
- plugins from a note

Alegorie Wolle -> Spinnen -> Weben -> Stricken
Alegorie Wiese: Fremde Samen fliegen ein

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
Wenn man einen guten Artikel `a` archivieren will, soll `a` nicht nur plump in eine Liste 
gesteckt werden. Gemäss dem Zettelkastenprinzip soll jeder Artikel mit mindestens einem anderen verlinkt werden mussen
um einen Kontext zu schaffen. Jeder Artikel im feed soll dann links anführen, unabhängig vom Content. 
Links: Artikel -> Artikel



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
        - basic auth
      -> plugin
    - prerender
      - postrender actions
        -> plugin
  - map
    - native
      - readability
      - audio/video stream
      - main image
      - score
    -> plugin
  - tetention policy (iff private)
  - reduce (iff private)
    - filter

Producer Events (Bucket)
- on trigger event
- filter (allocate article segment)
  - filter articles in segment
- map
  - native
    - add bucket tags
  -> plugin
- reduce
  - native
    - throttle
    - aggregate
  -> plugin
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
      - Content Quality Scoring
Producer
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

use cases
- website change
- router error log
- soundcloud likes digest
- archive soundcloud likes
- diff articles

from https://www.reddit.com/r/rss/comments/ppm9hh/looking_for_an_rssapp_alternative/
- Create an RSS feed for websites that do not have an RSS feed if it’s own and is able to grab the images that are associated with each new item.
- Pick what elements you want to show up in the feed.
- Create a new RSS feed for each feed created.
- Bundles RSS feeds together for a single link that will aggregate everything in the bundle.
- Tag white and black list.
- You can just follow a page, even if it does not look like a collection. In theory it is a collection of different versions over time. There should be a field in the article to store mapping content
- like readability. A mimeType for this field should also be specified.
