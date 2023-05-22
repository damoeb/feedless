package org.migor.feedless.data.jpa.seed

import jakarta.annotation.PostConstruct
import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.models.UserEntity
import org.migor.feedless.service.PropertyService
import org.migor.feedless.service.StatefulUserSecretService
import org.migor.feedless.service.UserService
import org.migor.feedless.util.CryptUtil.newCorrId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.Duration

@Service
@Profile("${AppProfiles.seed} && ${AppProfiles.database}")
class SeedFeeds {

  private lateinit var corrId: String
  private lateinit var rootUser: UserEntity
  private val log = LoggerFactory.getLogger(SeedFeeds::class.simpleName)

//  @Autowired
//  lateinit var bucketDAO: BucketDAO
//
//  @Autowired
//  lateinit var importerDAO: ImporterDAO
//
//  @Autowired
//  lateinit var streamDAO: StreamDAO
//
//  @Autowired
//  lateinit var genericFeedDAO: GenericFeedDAO
//
//  @Autowired
//  lateinit var feedDiscoveryService: FeedDiscoveryService
//
//  @Autowired
//  lateinit var nativeFeedService: NativeFeedService
//
//  @Autowired
//  lateinit var bucketService: BucketService
//
//  @Autowired
//  lateinit var filterService: FilterService

  @Autowired
  lateinit var userService: UserService

  @Autowired
  lateinit var userSecretService: StatefulUserSecretService

  @Autowired
  lateinit var propertyService: PropertyService

//  val harvestSite = false

  @PostConstruct
  @Transactional(propagation = Propagation.REQUIRED)
  fun postConstruct() {
    this.corrId = newCorrId()

//    val user = userService.getSystemUser()
    this.rootUser = userService.createUser("root", propertyService.rootEmail, true)
    userService.createUser("anonymous", propertyService.anonymousEmail, false)
    userSecretService.createSecretKey(propertyService.rootSecretKey, Duration.ofDays(356), rootUser)

//    createBucketForDanielDennet()
//    createBucketForAfterOn(user, corrId)
//    createBucketForBookworm(user, corrId)
//    createBucketForTeamHuman(user, corrId)
//    createBucketForRadiolab(user, corrId)
//    createBucketForMindscape(user, corrId)
  }
//
//  private fun createBucketForRadiolab() {
//    val bucket = bucketService.createBucket(
//      "",
//      title = "Radiolab Podcast",
//      description = "Radiolab Podcast",
//      visibility = EntityVisibility.isPublic,
//      user = rootUser
//    )
//    getNativeFeedForWebsite("Radiolab Podcast", "http://feeds.feedburner.com/radiolab", bucket)
//  }
//
//  private fun createBucketForAfterOn() {
//    val bucket = bucketService.createBucket(
//      "",
//      title = "After On Podcast",
//      description = "After On Podcast",
//      visibility = EntityVisibility.isPublic,
//      user = rootUser
//    )
//    getNativeFeedForWebsite("After On Podcast", "http://afteron.libsyn.com/rss", bucket)
//  }
//
//  private fun createBucketForTeamHuman() {
//    val bucket = bucketService.createBucket(
//      "",
//      title = "Team Human Podcast",
//      description = "Team Human Podcast",
//      visibility = EntityVisibility.isPublic,
//      user = rootUser
//    )
//    getNativeFeedForWebsite(
//      "Team Human Podcast",
//      "https://access.acast.com/rss/58ad887a1608b1752663b04a",
//      bucket,
//    )
//  }
//
//  private fun createBucketForBookworm() {
//    val bucket = bucketService.createBucket(
//      "",
//      title = "Bookworm Podcast",
//      description = "Bookworm Podcast",
//      visibility = EntityVisibility.isPublic,
//      user = rootUser
//    )
//    getNativeFeedForWebsite("Bookworm Podcast", "https://bookworm.fm/feed/podcast/", bucket)
//  }
//
//  private fun createBucketForMindscape() {
//    val bucket = bucketService.createBucket(
//      "",
//      title = "Mindscape Podcast",
//      description = "Mindscape Podcast",
//      visibility = EntityVisibility.isPublic,
//      user = rootUser
//    )
//    getNativeFeedForWebsite(
//      "Mindscape Podcast",
//      "https://rss.art19.com/sean-carrolls-mindscape",
//      bucket,
//    )
//  }
//
//
//  private fun createBucketForDanielDennet() {
//    val stream = streamDAO.save(StreamEntity())
//
//    val bucket = BucketEntity()
//    bucket.streamId = stream.id
//    bucket.title = "Daniel Dennet"
//    bucket.description = """Daniel Dennett received his D.Phil. in philosophy from Oxford University. He is currently
//        |Austin B. Fletcher Professor of Philosophy and co-director of the Center for Cognitive Studies at Tufts
//        |University. He is known for a number of philosophical concepts and coinages, including the intentional stance,
//        |the Cartesian theater, and the multiple-drafts model of consciousness. Among his honors are the Erasmus Prize,
//        |a Guggenheim Fellowship, and the American Humanist Association’s Humanist of the Year award. He is the author
//        |of a number of books that are simultaneously scholarly and popular, including Consciousness Explained, Darwin’s
//        |Dangerous Idea, and most recently Bacteria to Bach and Back.""".trimMargin()
//    bucket.owner = rootUser
//    val savedBucket = bucketDAO.save(bucket)
//    val hasLinksFilter = "linkCount > 0"
////    this.filterService.validateExpression(hasLinksFilter)
//    getGenericFeedForWebsite("Daniel Dennett Blog", "https://ase.tufts.edu/cogstud/dennett/recent.html", savedBucket, hasLinksFilter)
////    getGenericFeedForWebsite(
////      "Daniel Dennett Google Scholar",
////      "https://scholar.google.com/citations?user=3FWe5OQAAAAJ&hl=en",
////      savedBucket,
////      user
////    )
////    getFeedForLinksInWebsite("Daniel Dennett Wikipedia", "https://en.wikipedia.org/wiki/Daniel_Dennett", savedBucket)
////    getNativeFeedForWebsite(corrId, "Daniel Dennett Twitter", "https://twitter.com/danieldennett", savedBucket, true, user)
//    getGenericFeedForWebsite("The Clergy Project", "https://clergyproject.org/stories/", savedBucket)
////    getGenericFeedForWebsite("Center for Cognitive Studies", "https://ase.tufts.edu/cogstud/news.html", savedBucket)
//  }
//
//  private fun getNativeFeedForWebsite(
//    title: String,
//    websiteUrl: String,
//    bucket: BucketEntity,
//    filter: String = ""
//  ) {
//    val fetchOptions = FetchOptions(
//      websiteUrl
//    )
//    val feed = feedDiscoveryService.discoverFeeds(corrId, fetchOptions).results.nativeFeeds.first()
//    val nativeFeed =
//      nativeFeedService.createNativeFeed(corrId, title, "", feed.transient!!.url, websiteUrl, emptyList(), rootUser)
//    val importer = ImporterEntity()
//    importer.feed = nativeFeed
//    importer.bucket = bucket
//    importer.autoRelease = false
//    importer.owner = rootUser
//    importer.filter = filter
//    importerDAO.save(importer)
//  }
//
//  private fun getGenericFeedForWebsite(
//    title: String,
//    websiteUrl: String,
//    bucket: BucketEntity,
//    filter: String = ""
//  ) {
//    val corrId = ""
//    val discovery = feedDiscoveryService.discoverFeeds(corrId, FetchOptions(websiteUrl = websiteUrl))
//    val bestRule = discovery.results.genericFeedRules.first()
//    log.info("feedUrl ${bestRule.feedUrl}")
//    val nativeFeed = nativeFeedService.createNativeFeed(corrId, title, "", bestRule.feedUrl, websiteUrl, emptyList(), rootUser)
//
//    val genericFeed = GenericFeedEntity()
//    genericFeed.feedSpecification = GenericFeedSpecification(
//      selectors = GenericFeedSelectors(
//        linkXPath = bestRule.linkXPath,
//        extendContext = bestRule.extendContext,
//        contextXPath = bestRule.contextXPath,
//        dateXPath = bestRule.dateXPath,
//      ),
//      parserOptions = GenericFeedParserOptions(),
//      fetchOptions = FetchOptions(
//        websiteUrl = websiteUrl,
//        prerender = false,
//        prerenderWaitUntil = PuppeteerWaitUntil.load,
//        prerenderScript = ""
//      ),
//      refineOptions = GenericFeedRefineOptions(),
//    )
//    genericFeed.websiteUrl = websiteUrl
////    genericFeed.nativeFeed = nativeFeed
//
//    genericFeedDAO.save(genericFeed)
//
//
//    val importer = ImporterEntity()
//    importer.feed = nativeFeed
//    importer.bucket = bucket
//    importer.autoRelease = false
//    importer.owner = rootUser
//    importer.filter = filter
//    importerDAO.save(importer)
//  }
}
