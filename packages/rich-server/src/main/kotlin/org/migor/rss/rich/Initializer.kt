package org.migor.rss.rich

import com.google.gson.Gson
import com.google.gson.GsonBuilder
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

      val subscription = Subscription()
      subscription.name = "Twitter Armin Wolf"
      subscription.url = "https://twitter.com/ArminWolf"
      subscription.owner = user
      subscription.harvestFrequency = harvestFrequency
      subscription.nextHarvestAt = Date()

      val saved = subscriptionRepository.save(subscription)

      val gson: Gson = GsonBuilder().setPrettyPrinting().serializeNulls().create()

      log.info(gson.toJson(saved))
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
