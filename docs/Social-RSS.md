# Social RSS
`RSS` as the umbrella term for all the two web feed standards [ATOM](https://en.wikipedia.org/wiki/Atom_(Web_standard)) or [RSS](https://en.wikipedia.org/wiki/RSS_(file_format)) is the gold standard of the internet. Simple, extendable, less to no ads, almost no tracking, open, accessible;

`RSS` represents the open and federated internet, but its popularity faced a large decline in the last 10 years due to `Social Media` (SM) platforms. SM use a lot of dark patterns to keep you locked in and engaged, but to a certain degree there are really good, additional features that are not present yet in the web feed standard.

This article is a brainstorming, on how web feeds could be extended to support these features.

Due to network effects people will always be on several platforms, so its an utopia to think at some future point
of time all people will commit to the same plattform/standard especially if it is federated ([pleroma](https://pleroma.social/), [peertube](https://peer.tube/)).

As a premise to this document, I want to state that the current way of consuming feeds - which I think is 
 deeply flawed - should not be challenged here.

## Social Network is Gold
A network is at least one relationship between two or more entities, if it is a social network it is between 
people. In Social Networks this relationship is usually one-sided (unilateral), so by subscribing to a source
the relationship is established.

The value of the network lies in the curation of sources based on your individual interest. Curating is an
expensive and non-trivial process. Moreover, curating does not scale due to [Metcalfe's law](https://en.wikipedia.org/wiki/Metcalfe's_law). 
So it makes sense to socialize or outsource this cost to other individuals, which I call broker. That's
why we read news papers instead of talking or subscribing to every individual.

## Become a Broker
For the reasons mentioned above, you don't want to subscribe to too many individual sources, but rather 
rely on brokers. A broker usually shields his network of sources and simply passes pieces of information. 
A broker may curate on a source level or even on information level. The goal is that every user that wants
to share relevant information becomes a broker by exposing a feed.

## Individual RSS Broker Feed

A potential implementation using RSS faces some hurdles. 

### Challenge 1: Multiple Platforms
A person who wants to create a broker feed of 
all their curated sources already resides on several platforms, like HN, youtube or twitter. There
are solutions to turn popular third party website into feeds (nitter, rss-bridge, rss-proxy)

### Challenge 2: Signal to Noise Ratio
There is a threat of providing a useless feed, because one feed may cover too many fields of interest with the
consequence that people prefer not to subscribe in order to not-be spammed. Those fields may be 
- information relevant to your current locality
- information for entertainment purpose like music or videos
- highly context dependend data like comments or emotional statements

A large fraction of such an aggregated feed may useless to a general consumer, so either the curation
should be done on a piece-by-piece level or, the consumption method is wrong or if we want to keep
our feed readers, there should be sub-feeds, like flavors.

The classification into flavors shall not be a manual effort, cause this approach does not work as we know
from note taking. The most solid approach would be to expose the content curation done by the broker as
flavors.

| Url                          | Protected | Description                                                           |
|------------------------------|-----------|-----------------------------------------------------------------------|
| {user}/feed/private/likes    | Yes       | Articles the user favors. Synonyms: archive, like, retweet            |
| {user}/feed/public           | No        | All entries from whitelisted sources, no entry curation involved      |

Engagements `read` or `dislike` won't be exposed as a subfeed but back-propagated to the brokers/author.

On top of that there should be filter options based on the content. Go to `timeline` parapragh for more details. 

### Challenge 3: Spam
The amount of information people want to consume varies. Throttling the throughput of a feed is crucial.
This requires some quality/quantity measures, which can be used to distinguish good from bad.

### Challenge 3: Access-Restricted Feeds
There are people who procide and access restricted feeds. Same is true for platforms, that may require 
authentication before transforming them into a feed.

### Challenge 4: Private Sources
A general feed for everyone in the audience may become problematic, cause a broker may expose too
much private information. As mentioned before, there should be flavors of a feed, so the broker
 controls who sees what. 

### Challenge 5: Block Users
Every network has rules. In this unilateral network a broker defines the rules. Participating or profiting of a network always has the price of
adhering to the rules. For a broker it should be possible to kick others from the network and therefore blocking them. This implies that the
aggregated feed url is different for every subscription. 

### Challenge 6: Approve Subscriptions
In order to allow subscribers in your network and therefore beeing able to remove them as well, it is necessary to have some form of approval process
in place, like a temporarily valid secret they know. I can imagine a use-case, that at the end of a presentation you announce that you invite the audience
in a fragment of your network by sharing a qr code, that retrieves a feed url.

### Challenge 7: Analytics
Use [feedburner](http://www.feedburner.com/) as blueprint.

### Challenge 8: Privacy
Having a nonced feed url, that is directly associated to your user, you have pseudonymity. Accessing the feeds through a privacy-protecting reader the reader
has a tiny attact surface compared to JavaScript enabled, tracker invested sites.

### Challenge 9: Aggregated Feed as Master
The aggregated feed might just be used as a map/reduce proxy for data from different sites. More interesting is the idea of supporting it to be the master,
the other sites are just temporary sources, that might disappear.

Hence a content creater can publish directly to the aggregated feed.

### Challenge 10: Connected Networks and Transitive Bailing
A social network constructed from RSS will be fragmented. All popular social features rely on a non-fragmented network. Take a look at the following network, whereas `C` prefixes a pure consumer and `B` for broker.
```
C_1    C_2
  \   /
   B_1   C_3
     \   /
      B_2
```
Scenario: `C_1` writes a comment on an article, that originated in `B_2`, they are transitively connected. The goal is that `C_3` sees the comment, which requires `B_1` to forwar or better back-propagate that comment to `B_2`. `B_1` therefore bails for `C_1` and `B_2` will accept it. At some point a more elaborate way to manage participation in a network should be considered, that goes beyond the binary concept of all or nothing. Unfortunately this would need to keep `rich-rss` to keep a state for every user. Statelesness should be the goal though.

### Challenge 11: Server Lock-In, UIDs and Authorization
In a decentralized network there usually is no single truth. A user is connected to one `rich-rss` server, but it should not be relevant which one. On top of that a user must have a unique identifier he fully owns. I suggest using `sha($email)` as a global UIDs and `rich-rss` can authorize you to use a certain UID by sending a magic link to that email.  

### Challenge 12: Hide you subscribers


## Social Use Cases
Social Use Cases make a network social because they allow interaction. This shatters the producer/consumer classification of RSS. Due to the decentralized nature 
of an RSS based network, turing consumers into producer of stats, likes, comments et. al. is challenging cause the feed consumers are most likely not connected.
Let's look at this example. If a consumer Fred of a feed `n` decides to write a comment, it should be forwared to all other consumers of the same feed. All readers are indirectly connected via the feed broker, so forwarding engagements would require the broker to multiplex them. Since the broker is the gatekeeper that only allows trusted users to access the network and subscriptions can be canceled from both sides, automatic forwarding without a review process seems reasonable.  

A feed consumer might be a broker too, so forwarding engagements becomes more complex. A broker might define if engagements from a specific subscription are considered to be trusted or not.

Every feed entry can be extended by the `propagate` url, which will accept any kind of engagement listed below.

```
<?xml version="1.0" encoding="utf-8"?>
<feed xmlns="http://www.w3.org/2005/Atom">
  <srss:propagate>{baseUrl}/{subscriptionId}/propagate</srss:propagate>
...
</feed>
```

### Subscribe/Unsubscribe
Subscribing is the crucial entrypoint, the initiating action to establish a connection in a social network. This is - as it seems to me - quite hard in the RSS universe for unexperienced consumers. Social Media platforms got that right for both sides.

A subscriber might want to subscribe to engagements of fellow subscribers to the same broker, but not to third-party engagements. The data should be marked accordingly.

If we assume that there are only feeds that exist, there has to be a public one (`/feed/public`, that does not require authenication.

| Url                                      | Protected | Description                                                           |
|------------------------------------------|-----------|-----------------------------------------------------------------------|
| {user}/feed/subscribe                    | No        | Articles the user favors. Synonyms: archive or like                   |
| {user}/feed/unsubscribe/{subscriptionId} | Yes       | Articles the user wants to actively share. Synonyms: forward, retweet |

Calling `subscribe` will forward you to your unique subscription url to the feed, that works immediately. Initially the feed would render a welcome message and optionally 
additional steps to be approved.


### Timeline
This popular Social Media feature is usually the result of recommender machines. 

### (Nested) Comments
An implementation of comments is difficult to be implement inside a reader. It is better to render a link `Comments (4)` in the entry content that takes
you to the server, that renders the discussion properly.

### Like on Articles / Comment
`POST {user}/feed/propagate/like/{subscriptionId}/{entryId}?comment={commentId}`: adds entry to `private/likes` feed
As a fallback:
`GET {user}/feed/propagate/like/{subscriptionId}/{entryId}?comment={commentId}`: takes the user to the website with share options

### Read
If a user opens an entry, the reader should push the read event. There should be a fallback the way feedburner handels it, by proxiing the original url.
```
POST {user}/feed/propagate/read
{
  "subscription": {subscriptionId},
  "entry": {entryId}
}
```

### Share
I question that there is the need for a explicit share thread. 

`GET {user}/feed/propagate/share/{subscriptionId}/{entryId}`: takes you to the website with share options

### Mentions
TBD

### Propagate to Third-Party Services

## Adaptation Risks
I don't see a cold start problem. The initial effort to create an aggregated feed can be mitigated by importing OMPL files, importing the network from Twitter/yt et.al. 
