# Social RSS
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
A broker may curate on a source level or even on information level.

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


## Social Use Cases


### Subscribe/Unsubscribe
Subscribing is the crucial entrypoint, the initiating action to establish a connection in a social network. This is - as it seems to me - quite hard in the RSS universe for unexperienced consumers. Social Media platforms got that right for both sides.  

### Timeline

### Comment

### Read

### Like/Share/Archive
