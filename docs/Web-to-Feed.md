# Web to Feed

## Detect Feeds in DOM
Calling `/api/feeds/discover` will return the `FeedDiscovery` result that contains all generic and native feeds found in the markup.

## Extract a Feed

Call `/api/w2f` to apply the rules of a generic rule and receive a valid feed. If that transformation fails you will still receive a feed detailing the problems. 
