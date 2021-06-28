# Other
- subscription should have a size of items that it maximally contains
- other interesting sources are telegram groups, email
- preprocessing steps before running rss-proxy: click here and here...
- validation if feed is still working

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
- create bucket
- add feed to bucket
  - choose a feed from several potential feeds (native, rss-proxy, nitter)
  - see a feeds entries
- extend feed entries
- set bucket retention policy: timeless | short-lived
- throttle a subscription(source)
- filter entires a bucket
    

- inbox/queue for your private feed

check https://github.com/converspace/webmention/blob/master/README.md

- feed ui like apple podcasts https://podcasts.apple.com/us/podcast/stuff-you-should-know/id278981407
    or https://philpeople.org/profiles/dominique-kuenzle
- restore created date using archive.org first harvest or correct it with1 week distance each
- readability ui
- add missing filters videoCount, audioCount
- user points
- user like/comment

Alegorie Wolle -> Spinnen -> Weben -> Stricken
Alegorie Wiese: Fremde Samen fliegen ein

# Archive Funktion
Wenn man einen guten Artikel `a` archivieren will, soll `a` nicht nur plump in eine Liste 
gesteckt werden. Gemäss dem Zettelkastenprinzip soll jeder Artikel mit mindestens einem anderen verlinkt werden mussen
um einen Kontext zu schaffen. Jeder Artikel im feed soll dann links anführen, unabhängig vom Content. 
Links: Artikel -> Artikel

# Wording
Im moment wird viel entry verwendet, aber das ist sehr feed spezifisch. `Document` wäre besser.
