package org.migor.rich.rss.database2

import org.migor.rich.rss.database2.models.BucketEntity
import org.migor.rich.rss.database2.models.FeedManagerType
import org.migor.rich.rss.database2.models.GenericFeedEntity
import org.migor.rich.rss.database2.models.GenericFeedStatus
import org.migor.rich.rss.database2.models.NativeFeedEntity
import org.migor.rich.rss.database2.models.NativeFeedStatus
import org.migor.rich.rss.database2.models.StreamEntity
import org.migor.rich.rss.database2.models.TagEntity
import org.migor.rich.rss.database2.models.TagType
import org.migor.rich.rss.database2.models.UserEntity
import org.migor.rich.rss.database2.repositories.BucketDAO
import org.migor.rich.rss.database2.repositories.GenericFeedDAO
import org.migor.rich.rss.database2.repositories.NativeFeedDAO
import org.migor.rich.rss.database2.repositories.StreamDAO
import org.migor.rich.rss.database2.repositories.TagDAO
import org.migor.rich.rss.database2.repositories.UserDAO
import org.migor.rich.rss.discovery.FeedDiscoveryService
import org.migor.rich.rss.transform.WebToFeedService
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
  lateinit var streamDAO: StreamDAO

  @Autowired
  lateinit var nativeFeedDAO: NativeFeedDAO

  @Autowired
  lateinit var genericFeedDAO: GenericFeedDAO

  @Autowired
  lateinit var tagDAO: TagDAO

  @Autowired
  lateinit var webToFeedService: WebToFeedService

  @Autowired
  lateinit var feedDiscoveryService: FeedDiscoveryService

  @PostConstruct
  @Transactional(propagation = Propagation.REQUIRED)
  fun postConstruct() {
    tagDAO.saveAll(listOf("article", "media", "short").map { TagEntity(TagType.CONTENT, it) })

    val user = UserEntity()
    userDAO.save(user)

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


    bucket.feeds = mutableListOf(
      getFeedForWebsite("Daniel Dennett Blog", "https://ase.tufts.edu/cogstud/dennett/recent.html"),
      getFeedForWebsite("Daniel Dennett Google Scholar", "https://scholar.google.com/citations?user=3FWe5OQAAAAJ&hl=en"),
//      getFeedForLinksInWebsite("Daniel Dennett Wikipedia", "https://en.wikipedia.org/wiki/Daniel_Dennett"),
      getFeedForWebsite("Daniel Dennett Twitter","https://twitter.com/danieldennett"),
      getFeedForWebsite("The Clergy Project", "https://clergyproject.org/stories//"),
      getFeedForWebsite("Center for Cognitive Studies", "https://ase.tufts.edu/cogstud/news.html")
    )

    bucketDAO.save(bucket)
  }

  private fun getFeedForWebsite(title: String, websiteUrl: String): NativeFeedEntity {
    val corrId = ""
    val bestRule = feedDiscoveryService.discoverFeeds(corrId, websiteUrl).results.genericFeedRules.first()
    val feedRule = feedDiscoveryService.asExtendedRule(corrId, websiteUrl, bestRule)

    val nativeFeedEntity = NativeFeedEntity()
    nativeFeedEntity.title = title
    nativeFeedEntity.feedUrl = feedRule.feedUrl
    nativeFeedEntity.domain = URL(websiteUrl).host
    nativeFeedEntity.websiteUrl = websiteUrl
    nativeFeedEntity.managedBy = FeedManagerType.GENERIC_FEED
    nativeFeedEntity.status = NativeFeedStatus.DEACTIVATED

    val nativeFeed = nativeFeedDAO.save(nativeFeedEntity)


    val genericFeed = GenericFeedEntity()
    genericFeed.feedRule = feedRule
    genericFeed.managingFeed = nativeFeed
    genericFeed.status = GenericFeedStatus.OK
    genericFeed.tags

    genericFeedDAO.save(genericFeed)

    return nativeFeed
  }
}
