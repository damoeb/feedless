package org.migor.rss.rich

import org.migor.rss.rich.harvest.FilterOperators
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
      harvestFrequency.timeUnit = ChronoUnit.HOURS
      harvestFrequency.intervalValue = 2
      harvestFrequencyRepository.save(harvestFrequency)

//      val subscription1 = Subscription()
//      subscription1.name = "Twitter on Armin Wolf"
//      subscription1.url = "https://twitter.com/ArminWolf"
//      subscription1.owner = user
//      subscription1.harvestFrequency = harvestFrequency
//      subscription1.nextHarvestAt = Date()
//      subscription1.feedSize = 10
//      subscriptionRepository.save(subscription1)

      val subscription2 = Subscription()
      subscription2.name = "Daniel Lemire's Blog"
      subscription2.url = "https://lemire.me/blog/feed/"
      subscription2.withFullText = true
      subscription2.filter = listOf(Triple("title", FilterOperators.CONTAINS, "Science and Technology"))

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
