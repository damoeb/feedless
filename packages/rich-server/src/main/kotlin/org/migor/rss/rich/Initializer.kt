package org.migor.rss.rich

import org.migor.rss.rich.feed.FeedResolver
import org.migor.rss.rich.model.EntryRetentionPolicy
import org.migor.rss.rich.model.HarvestFrequency
import org.migor.rss.rich.model.Subscription
import org.migor.rss.rich.model.User
import org.migor.rss.rich.repository.HarvestFrequencyRepository
import org.migor.rss.rich.repository.SubscriptionRepository
import org.migor.rss.rich.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.concurrent.TimeUnit
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
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var subscriptionRepository: SubscriptionRepository

    @PostConstruct
    fun init() {
      val user = User()
      user.apiKey = "foo"
      userRepository.save(user)

      val harvestFrequency = HarvestFrequency()
      harvestFrequency.timeUnit = TimeUnit.HOURS
      harvestFrequency.intervalValue = 2
      harvestFrequencyRepository.save(harvestFrequency)

//      -- Twitter
      val subscription = Subscription()
      subscription.name = "Twitter Armin Wolf"
      subscription.url = "https://twitter.com/ArminWolf"
      subscription.owner = user
      subscription.harvestFrequency = harvestFrequency
      subscription.nextHarvestAt = Date()
      val (sourceType, feedType1, rssProxyUrl) = FeedResolver.resolve(subscription)
      subscription.sourceType = sourceType
      subscription.rssProxyUrl = rssProxyUrl
      subscription.throttled = true
      subscription.releaseInterval = 10
      subscription.releaseTimeunit = ChronoUnit.MINUTES
      subscription.releaseBatchSize = 10
      subscription.retentionPolicy = EntryRetentionPolicy.ARCHIVE

      subscriptionRepository.save(subscription)


//    -- Rss Feed
      val subscription2 = Subscription()
      subscription2.name = "Daniel Lemire's Blog"
      subscription2.url = "https://lemire.me/blog/feed/"
      subscription2.harvestFrequency = harvestFrequency
      subscription2.nextHarvestAt = Date()
      val (sourceType2, feedType2, rssProxyUrl2) = FeedResolver.resolve(subscription2)
      subscription2.sourceType = sourceType2
      subscription2.rssProxyUrl = rssProxyUrl2
//      subscription2.filter = listOf(Triple("title", FilterOperators.CONTAINS, "Science and Technology"))
      subscriptionRepository.save(subscription2)
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
