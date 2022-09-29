package org.migor.rich.rss.database2

import org.migor.rich.rss.database2.models.BucketEntity
import org.migor.rich.rss.database2.models.ExporterEntity
import org.migor.rich.rss.database2.models.FeedManagerType
import org.migor.rich.rss.database2.models.GenericFeedEntity
import org.migor.rich.rss.database2.models.GenericFeedStatus
import org.migor.rich.rss.database2.models.NativeFeedEntity
import org.migor.rich.rss.database2.models.NativeFeedStatus
import org.migor.rich.rss.database2.models.StreamEntity
import org.migor.rich.rss.database2.models.SubscriptionEntity
import org.migor.rich.rss.database2.models.TagEntity
import org.migor.rich.rss.database2.models.TagType
import org.migor.rich.rss.database2.models.UserEntity
import org.migor.rich.rss.database2.repositories.BucketDAO
import org.migor.rich.rss.database2.repositories.ExporterDAO
import org.migor.rich.rss.database2.repositories.GenericFeedDAO
import org.migor.rich.rss.database2.repositories.NativeFeedDAO
import org.migor.rich.rss.database2.repositories.StreamDAO
import org.migor.rich.rss.database2.repositories.SubscriptionDAO
import org.migor.rich.rss.database2.repositories.TagDAO
import org.migor.rich.rss.database2.repositories.UserDAO
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
    val tags = tagDAO.saveAll(listOf("read", "short").map { TagEntity(TagType.CONTENT, it) })

//    tags.find { tag -> tag.name === name }
    val toTagEntity = { name:String -> tags.find { tag -> tag.name === name }}

    val user = UserEntity()
    user.name = "system"
    val savedUser = userDAO.save(user)

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
    val savedBucket = bucketDAO.save(bucket)

    val exporter = ExporterEntity()
    exporter.bucket = savedBucket
    exporterDAO.save(exporter)

    val tagEntities = listOf("read").mapNotNull { name -> toTagEntity(name) }
      getGenericFeedForWebsite("Daniel Dennett Blog", "https://ase.tufts.edu/cogstud/dennett/recent.html", tagEntities, savedBucket)
      getGenericFeedForWebsite("Daniel Dennett Google Scholar", "https://scholar.google.com/citations?user=3FWe5OQAAAAJ&hl=en", tagEntities, savedBucket)
//      getFeedForLinksInWebsite("Daniel Dennett Wikipedia", "https://en.wikipedia.org/wiki/Daniel_Dennett", tagEntities, savedBucket),
      getNativeFeedForWebsite(corrId, "Daniel Dennett Twitter","https://twitter.com/danieldennett", listOf("read", "short").mapNotNull { name -> toTagEntity(name) }, savedBucket)
      getGenericFeedForWebsite("The Clergy Project", "https://clergyproject.org/stories/", tagEntities, savedBucket)
      getGenericFeedForWebsite("Center for Cognitive Studies", "https://ase.tufts.edu/cogstud/news.html", tagEntities, savedBucket)
  }

  private fun getNativeFeedForWebsite(corrId: String, title: String, websiteUrl: String, tags: List<TagEntity>, bucket: BucketEntity) {
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

  private fun getGenericFeedForWebsite(title: String, websiteUrl: String, tags: List<TagEntity>, bucket: BucketEntity) {
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
    genericFeed.tags = tags

    genericFeedDAO.save(genericFeed)


    val subscription = SubscriptionEntity()
    subscription.feed = savedNativeFeed
    subscription.bucket = bucket
    subscriptionDAO.save(subscription)
  }
}
