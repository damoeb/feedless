package org.migor.rss.rich

import org.migor.rss.rich.model.*
import org.migor.rss.rich.repository.*
import org.migor.rss.rich.service.FeedService
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
    lateinit var subscriptionRepository: SubscriptionRepository

    @Autowired
    lateinit var subscriptionGroupRepository: SubscriptionGroupRepository

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var sourceRepository: SourceRepository

    @Autowired
    lateinit var feedService: FeedService

    @Autowired
    lateinit var feedRepository: FeedRepository

    @PostConstruct
    fun init() {

      var user1 = User()
      user1.apiKey = "key1"
      user1.name = "foo"
      user1.emailHash = CryptUtil.sha1("foo@bar")
      user1.description = "Senior Software Engineering"
      userRepository.save(user1)

      user1.feeds = listOf(
        feedRepository.save(Feed("private", user1)),
        feedRepository.save(Feed("public", user1))
      )
      user1 = userRepository.save(user1)

//      val se = SourceEntry()
//      se.
//      FeedEntry()
//      feedService.addEntry(user1, "Boo this is a comment")

      var user2 = User()
      user2.apiKey = "key2"
      user2.name = "bar"
      user2.emailHash = CryptUtil.sha1("bar@foo")
      user2.description = "Learning | Focussing | Living"
      user2 = userRepository.save(user2)

      user2.feeds = listOf(
        feedRepository.save(Feed("private", user2)),
        feedRepository.save(Feed("public", user2))
      )
      userRepository.save(user2)

      val source1 = sourceRepository.save(Source("https://lemire.me/blog/feed/"))
      val source2 = sourceRepository.save(Source("https://www.lesswrong.com/feed.xml?view=curated-rss"))
      val sub1 = subscriptionRepository.save(Subscription(user1, source1))
      val subscription = Subscription(user1, source2)
      subscription.throttled = true
      subscription.releaseBatchSize = 2
      subscription.releaseInterval = 1
      subscription.releaseTimeUnit = ChronoUnit.DAYS
      val sub2 = subscriptionRepository.save(subscription)
      val group1 = subscriptionGroupRepository.save(SubscriptionGroup("tech", user1))
      sub1.group = group1
      subscriptionRepository.save(sub1)
      sub2.group = group1
      subscriptionRepository.save(sub2)

      val source3 = sourceRepository.save(Source("http://brett.trpstra.net/brettterpstra"))
      val sub3 = subscriptionRepository.save(Subscription(user2, source3))
      val source4 = sourceRepository.save(Source("https://www.heise.de/rss/heise.rdf"))
      val sub4 = subscriptionRepository.save(Subscription(user2, source4))
      val source5 = sourceRepository.save(Source("https://www.heise.de/tp/news-atom.xml"))
      val sub5 = subscriptionRepository.save(Subscription(user2, source5))

      val group2 = subscriptionGroupRepository.save(SubscriptionGroup("tech", user2))
      sub3.group = group2
      subscriptionRepository.save(sub3)
      val group3 = subscriptionGroupRepository.save(SubscriptionGroup("news", user2))
//      sub4.group = group3
//      subscriptionRepository.save(sub3)
      sub5.group = group3
      subscriptionRepository.save(sub5)
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
