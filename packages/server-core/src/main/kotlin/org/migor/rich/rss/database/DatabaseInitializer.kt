package org.migor.rich.rss.database

import org.migor.rich.rss.database.models.BucketEntity
import org.migor.rich.rss.database.models.ExporterEntity
import org.migor.rich.rss.database.models.FeedManagerType
import org.migor.rich.rss.database.models.GenericFeedEntity
import org.migor.rich.rss.database.models.GenericFeedStatus
import org.migor.rich.rss.database.models.NativeFeedEntity
import org.migor.rich.rss.database.models.NativeFeedStatus
import org.migor.rich.rss.database.models.StreamEntity
import org.migor.rich.rss.database.models.SubscriptionEntity
import org.migor.rich.rss.database.models.TagEntity
import org.migor.rich.rss.database.models.TagType
import org.migor.rich.rss.database.models.UserEntity
import org.migor.rich.rss.database.repositories.BucketDAO
import org.migor.rich.rss.database.repositories.ExporterDAO
import org.migor.rich.rss.database.repositories.GenericFeedDAO
import org.migor.rich.rss.database.repositories.NativeFeedDAO
import org.migor.rich.rss.database.repositories.StreamDAO
import org.migor.rich.rss.database.repositories.SubscriptionDAO
import org.migor.rich.rss.database.repositories.TagDAO
import org.migor.rich.rss.database.repositories.UserDAO
import org.migor.rich.rss.discovery.FeedDiscoveryService
import org.migor.rich.rss.util.CryptUtil.newCorrId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.net.URL
import javax.annotation.PostConstruct

@Service
class DatabaseInitializer {
  @Autowired
  lateinit var userDAO: UserDAO

  @Autowired
  lateinit var bucketDAO: BucketDAO

  @Autowired
  lateinit var exporterDAO: ExporterDAO

  @Autowired
  lateinit var streamDAO: StreamDAO

  @Autowired
  lateinit var nativeFeedDAO: NativeFeedDAO

  @Autowired
  lateinit var genericFeedDAO: GenericFeedDAO

  @Autowired
  lateinit var tagDAO: TagDAO

  @Autowired
  lateinit var subscriptionDAO: SubscriptionDAO

  @Autowired
  lateinit var feedDiscoveryService: FeedDiscoveryService

  @PostConstruct
  @Transactional(propagation = Propagation.REQUIRED)
  fun postConstruct() {
    val corrId = newCorrId()

    tagDAO.saveAll(listOf("person", "podcast").map { TagEntity(TagType.CONTENT, it) })

    val user = UserEntity()
    user.name = "system"
    val savedUser = userDAO.save(user)

//    createBucketForDanielDennet(savedUser, corrId)
    createBucketForAfterOn(savedUser, corrId)
  }

  private fun createBucketForAfterOn(savedUser: UserEntity, corrId: String) {
    val stream = streamDAO.save(StreamEntity())

    val bucket = BucketEntity()
    bucket.stream = stream
    bucket.name = "After On Podcast"
    bucket.description = """""".trimMargin()
    bucket.owner = savedUser
    bucket.tags = arrayOf("podcast").map { tagDAO.findByNameAndType(it, TagType.CONTENT) }
    val savedBucket = bucketDAO.save(bucket)

    val exporter = ExporterEntity()
    exporter.bucket = savedBucket
    exporterDAO.save(exporter)

    getNativeFeedForWebsite(corrId, "After On Podcast", "http://afteron.libsyn.com/rss", savedBucket)
  }

  private fun createBucketForDanielDennet(savedUser: UserEntity, corrId: String) {
    val stream = streamDAO.save(StreamEntity())

    val bucket = BucketEntity()
    bucket.stream = stream
    bucket.name = "Daniel Dennet"
    bucket.description = """Daniel Dennett received his D.Phil. in philosophy from Oxford University. He is currently
        |Austin B. Fletcher Professor of Philosophy and co-director of the Center for Cognitive Studies at Tufts
        |University. He is known for a number of philosophical concepts and coinages, including the intentional stance,
        |the Cartesian theater, and the multiple-drafts model of consciousness. Among his honors are the Erasmus Prize,
        |a Guggenheim Fellowship, and the American Humanist Association’s Humanist of the Year award. He is the author
        |of a number of books that are simultaneously scholarly and popular, including Consciousness Explained, Darwin’s
        |Dangerous Idea, and most recently Bacteria to Bach and Back.""".trimMargin()
    bucket.owner = savedUser
    bucket.tags = arrayOf("person").map { tagDAO.findByNameAndType(it, TagType.CONTENT) }
    val savedBucket = bucketDAO.save(bucket)

    val exporter = ExporterEntity()
    exporter.bucket = savedBucket
    exporterDAO.save(exporter)

    getGenericFeedForWebsite("Daniel Dennett Blog", "https://ase.tufts.edu/cogstud/dennett/recent.html", savedBucket)
    getGenericFeedForWebsite(
      "Daniel Dennett Google Scholar",
      "https://scholar.google.com/citations?user=3FWe5OQAAAAJ&hl=en",
      savedBucket
    )
    //      getFeedForLinksInWebsite("Daniel Dennett Wikipedia", "https://en.wikipedia.org/wiki/Daniel_Dennett", savedBucket),
    getNativeFeedForWebsite(corrId, "Daniel Dennett Twitter", "https://twitter.com/danieldennett", savedBucket)
    getGenericFeedForWebsite("The Clergy Project", "https://clergyproject.org/stories/", savedBucket)
    getGenericFeedForWebsite("Center for Cognitive Studies", "https://ase.tufts.edu/cogstud/news.html", savedBucket)
  }

  private fun getNativeFeedForWebsite(corrId: String, title: String, websiteUrl: String, bucket: BucketEntity) {
    val feed = feedDiscoveryService.discoverFeeds(corrId, websiteUrl).results.nativeFeeds.first()

    val stream = streamDAO.save(StreamEntity())

    val nativeFeed = NativeFeedEntity()
    nativeFeed.title = title
    nativeFeed.feedUrl = feed.url
    nativeFeed.domain = URL(websiteUrl).host
    nativeFeed.websiteUrl = websiteUrl
    nativeFeed.managedBy = FeedManagerType.GENERIC_FEED
    nativeFeed.status = NativeFeedStatus.OK
    nativeFeed.stream = stream
    nativeFeed.harvestSite = false

    val savedNativeFeed = nativeFeedDAO.save(nativeFeed)

    val subscription = SubscriptionEntity()
    subscription.feed = savedNativeFeed
    subscription.bucket = bucket
    subscriptionDAO.save(subscription)
  }

  private fun getGenericFeedForWebsite(title: String, websiteUrl: String, bucket: BucketEntity) {
    val corrId = ""
    val bestRule = feedDiscoveryService.discoverFeeds(corrId, websiteUrl).results.genericFeedRules.first()
    val feedRule = feedDiscoveryService.asExtendedRule(corrId, websiteUrl, bestRule)

    val stream = streamDAO.save(StreamEntity())

    val nativeFeed = NativeFeedEntity()
    nativeFeed.title = title
    nativeFeed.feedUrl = feedRule.feedUrl
    nativeFeed.domain = URL(websiteUrl).host
    nativeFeed.websiteUrl = websiteUrl
    nativeFeed.managedBy = FeedManagerType.GENERIC_FEED
    nativeFeed.status = NativeFeedStatus.OK
    nativeFeed.stream = stream
    nativeFeed.harvestSite = true

    val savedNativeFeed = nativeFeedDAO.save(nativeFeed)

    val genericFeed = GenericFeedEntity()
    genericFeed.feedRule = feedRule
    genericFeed.managingFeed = savedNativeFeed
    genericFeed.status = GenericFeedStatus.OK

    genericFeedDAO.save(genericFeed)


    val subscription = SubscriptionEntity()
    subscription.feed = savedNativeFeed
    subscription.bucket = bucket
    subscriptionDAO.save(subscription)
  }
}
