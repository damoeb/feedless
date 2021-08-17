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
- content resolution using webhooks
- set bucket retention policy: timeless | short-lived
- throttle a subscription(source)
- filter entires in a bucket
- Schnäppchen Suche? custom attributes
- if no native feed is exposed, test if website is wordpress and guess url
- preview image? No
- subscription
- follow my profile
- query bing.com/search?format=rss&q=khayrirrw, hn
- how does social stuff work?
- create a closed source pj for scoring and graph
- editing the filter should render a diff of articles
- inbox/queue for your private feed
check https://github.com/converspace/webmention/blob/master/README.md

- use kotlin linter https://blog.mindorks.com/code-formatting-in-kotlin-using-ktlint
- how to sell to researchers:
  - working on paper x -> defines bucket
  - create notes

- feed ui like apple podcasts https://podcasts.apple.com/us/podcast/stuff-you-should-know/id278981407
    or https://philpeople.org/profiles/dominique-kuenzle
- add missing filters videoCount, audioCount
- quality scoring
- plugins from a note
- user points
- user like/comment

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
