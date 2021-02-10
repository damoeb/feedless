package org.migor.rss.rich

import org.migor.rss.rich.model.HarvestFrequency
import org.migor.rss.rich.model.Source
import org.migor.rss.rich.model.Subscription
import org.migor.rss.rich.model.User
import org.migor.rss.rich.repository.HarvestFrequencyRepository
import org.migor.rss.rich.repository.SourceRepository
import org.migor.rss.rich.repository.SubscriptionRepository
import org.migor.rss.rich.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.time.temporal.ChronoUnit
import javax.annotation.PostConstruct


@Configuration
class Initializer {

  @Component
  @Profile("dev")
  class DevInitializer {

    val log = LoggerFactory.getLogger("DevInitializer")

    @Autowired
    lateinit var harvestFrequencyRepository: HarvestFrequencyRepository

    @Autowired
    lateinit var subscriptionRepository: SubscriptionRepository

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var sourceRepository: SourceRepository

    @PostConstruct
    fun init() {
      val harvestFrequency = HarvestFrequency()
      harvestFrequency.timeUnit = ChronoUnit.MINUTES
      harvestFrequency.intervalValue = 5
      harvestFrequencyRepository.save(harvestFrequency)

      val user1 = User()
      user1.apiKey = "key1"
      user1.name = "foo"
      user1.emailHash = CryptUtil.sha1("foo@bar")
      user1.description = "Senior Software Engineering"
      userRepository.save(user1)

      val user2 = User()
      user2.apiKey = "key2"
      user2.name = "bar"
      user2.emailHash = CryptUtil.sha1("bar@foo")
      user2.description = "Learning | Focussing | Living"
      userRepository.save(user2)

////      -- Twitter
//      val subscription = Subscription()
//      subscription.name = "Twitter Armin Wolf"
//      subscription.url = "https://twitter.com/ArminWolf"
//      subscription.owner = user
//      subscription.harvestFrequency = harvestFrequency
//      subscription.nextHarvestAt = Date()
//      val (sourceType, feedType1, rssProxyUrl) = FeedResolver.resolve(subscription)
//      subscription.sourceType = sourceType
//      subscription.rssProxyUrl = rssProxyUrl
//      subscription.throttled = true
//      subscription.releaseInterval = 10
//      subscription.releaseTimeUnit = ChronoUnit.MINUTES
//      subscription.releaseBatchSize = 5
//      subscription.retentionPolicy = EntryRetentionPolicy.ARCHIVE
//
//      subscriptionRepository.save(subscription)


//    -- Rss Feed
      val source1 = sourceRepository.save(Source("https://lemire.me/blog/feed/"))
      val source2 = sourceRepository.save(Source("https://www.lesswrong.com/feed.xml?view=curated-rss"))
      val sub1 = subscriptionRepository.save(Subscription(user1, source1))
      val sub2 = subscriptionRepository.save(Subscription(user1, source2))
      user1.subscriptions = listOf(sub1, sub2)
      userRepository.save(user1)

      val source3 = sourceRepository.save(Source("http://brett.trpstra.net/brettterpstra"))
      val sub3 = subscriptionRepository.save(Subscription(user2, source3))
      val source4 = sourceRepository.save(Source("https://www.heise.de/rss/heise.rdf"))
      val sub4 = subscriptionRepository.save(Subscription(user2, source4))
      val source5 = sourceRepository.save(Source("https://www.heise.de/tp/news-atom.xml"))
      val sub5 = subscriptionRepository.save(Subscription(user2, source5))
      user2.subscriptions = listOf(sub3, sub4, sub5)
      userRepository.save(user2)

//    -- JSON Feed
//    https://jsonfeed.org/feed.json
    }

  }

  @Component
  @Profile("dev")
  class ProdInitializer {

    @PostConstruct
    fun init() {
      // todo create admin, ..
    }

  }
}
