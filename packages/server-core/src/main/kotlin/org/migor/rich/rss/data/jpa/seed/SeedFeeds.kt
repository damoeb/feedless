package org.migor.rich.rss.data.jpa.seed

import jakarta.annotation.PostConstruct
import org.migor.rich.rss.AppProfiles
import org.migor.rich.rss.data.jpa.enums.EntityVisibility
import org.migor.rich.rss.data.jpa.models.BucketEntity
import org.migor.rich.rss.data.jpa.models.GenericFeedEntity
import org.migor.rich.rss.data.jpa.models.ImporterEntity
import org.migor.rich.rss.data.jpa.models.StreamEntity
import org.migor.rich.rss.data.jpa.models.UserEntity
import org.migor.rich.rss.data.jpa.repositories.BucketDAO
import org.migor.rich.rss.data.jpa.repositories.GenericFeedDAO
import org.migor.rich.rss.data.jpa.repositories.ImporterDAO
import org.migor.rich.rss.data.jpa.repositories.StreamDAO
import org.migor.rich.rss.discovery.FeedDiscoveryService
import org.migor.rich.rss.service.BucketService
import org.migor.rich.rss.service.FilterService
import org.migor.rich.rss.service.NativeFeedService
import org.migor.rich.rss.service.PropertyService
import org.migor.rich.rss.service.UserService
import org.migor.rich.rss.transform.GenericFeedFetchOptions
import org.migor.rich.rss.transform.GenericFeedParserOptions
import org.migor.rich.rss.transform.GenericFeedRefineOptions
import org.migor.rich.rss.transform.GenericFeedSelectors
import org.migor.rich.rss.transform.GenericFeedSpecification
import org.migor.rich.rss.transform.PuppeteerWaitUntil
import org.migor.rich.rss.util.CryptUtil.newCorrId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
@Profile("${AppProfiles.bootstrap} && ${AppProfiles.database}")
class SeedFeeds {

  private lateinit var corrId: String
  private lateinit var user: UserEntity
  private val log = LoggerFactory.getLogger(SeedFeeds::class.simpleName)

  @Autowired
  lateinit var bucketDAO: BucketDAO

  @Autowired
  lateinit var importerDAO: ImporterDAO

  @Autowired
  lateinit var streamDAO: StreamDAO

  @Autowired
  lateinit var genericFeedDAO: GenericFeedDAO

  @Autowired
  lateinit var feedDiscoveryService: FeedDiscoveryService

  @Autowired
  lateinit var nativeFeedService: NativeFeedService

  @Autowired
  lateinit var bucketService: BucketService

  @Autowired
  lateinit var filterService: FilterService

  @Autowired
  lateinit var userService: UserService

  @Autowired
  lateinit var propertyService: PropertyService

  val harvestSite = false

  @PostConstruct
  @Transactional(propagation = Propagation.REQUIRED)
  fun postConstruct() {
    this.corrId = newCorrId()

//    val user = userService.getSystemUser()
    this.user = userService.createUser("root", propertyService.rootEmail, propertyService.rootSecretKey, true)

    createBucketForDanielDennet()
//    createBucketForAfterOn(user, corrId)
//    createBucketForBookworm(user, corrId)
//    createBucketForTeamHuman(user, corrId)
//    createBucketForRadiolab(user, corrId)
//    createBucketForMindscape(user, corrId)
  }

  private fun createBucketForRadiolab() {
    val bucket = bucketService.createBucket(
      "",
      title = "Radiolab Podcast",
      description = "Radiolab Podcast",
      visibility = EntityVisibility.isPublic,
      user = user
    )
    getNativeFeedForWebsite("Radiolab Podcast", "http://feeds.feedburner.com/radiolab", bucket, harvestSite)
  }

  private fun createBucketForAfterOn() {
    val bucket = bucketService.createBucket(
      "",
      title = "After On Podcast",
      description = "After On Podcast",
      visibility = EntityVisibility.isPublic,
      user = user
    )
    getNativeFeedForWebsite("After On Podcast", "http://afteron.libsyn.com/rss", bucket, harvestSite)
  }

  private fun createBucketForTeamHuman() {
    val bucket = bucketService.createBucket(
      "",
      title = "Team Human Podcast",
      description = "Team Human Podcast",
      visibility = EntityVisibility.isPublic,
      user = user
    )
    getNativeFeedForWebsite(
      "Team Human Podcast",
      "https://access.acast.com/rss/58ad887a1608b1752663b04a",
      bucket,
      harvestSite,
    )
  }

  private fun createBucketForBookworm() {
    val bucket = bucketService.createBucket(
      "",
      title = "Bookworm Podcast",
      description = "Bookworm Podcast",
      visibility = EntityVisibility.isPublic,
      user = user
    )
    getNativeFeedForWebsite("Bookworm Podcast", "https://bookworm.fm/feed/podcast/", bucket, harvestSite)
  }

  private fun createBucketForMindscape() {
    val bucket = bucketService.createBucket(
      "",
      title = "Mindscape Podcast",
      description = "Mindscape Podcast",
      visibility = EntityVisibility.isPublic,
      user = user
    )
    getNativeFeedForWebsite(
      "Mindscape Podcast",
      "https://rss.art19.com/sean-carrolls-mindscape",
      bucket,
      harvestSite,
    )
  }


  private fun createBucketForDanielDennet() {
    val stream = streamDAO.save(StreamEntity())

    val bucket = BucketEntity()
    bucket.stream = stream
    bucket.title = "Daniel Dennet"
    bucket.description = """Daniel Dennett received his D.Phil. in philosophy from Oxford University. He is currently
        |Austin B. Fletcher Professor of Philosophy and co-director of the Center for Cognitive Studies at Tufts
        |University. He is known for a number of philosophical concepts and coinages, including the intentional stance,
        |the Cartesian theater, and the multiple-drafts model of consciousness. Among his honors are the Erasmus Prize,
        |a Guggenheim Fellowship, and the American Humanist Association’s Humanist of the Year award. He is the author
        |of a number of books that are simultaneously scholarly and popular, including Consciousness Explained, Darwin’s
        |Dangerous Idea, and most recently Bacteria to Bach and Back.""".trimMargin()
    bucket.owner = user
    val savedBucket = bucketDAO.save(bucket)
    val hasLinksFilter = "linkCount > 0"
//    this.filterService.validateExpression(hasLinksFilter)
    getGenericFeedForWebsite("Daniel Dennett Blog", "https://ase.tufts.edu/cogstud/dennett/recent.html", savedBucket, hasLinksFilter)
//    getGenericFeedForWebsite(
//      "Daniel Dennett Google Scholar",
//      "https://scholar.google.com/citations?user=3FWe5OQAAAAJ&hl=en",
//      savedBucket,
//      user
//    )
//    getFeedForLinksInWebsite("Daniel Dennett Wikipedia", "https://en.wikipedia.org/wiki/Daniel_Dennett", savedBucket)
//    getNativeFeedForWebsite(corrId, "Daniel Dennett Twitter", "https://twitter.com/danieldennett", savedBucket, true, user)
    getGenericFeedForWebsite("The Clergy Project", "https://clergyproject.org/stories/", savedBucket)
//    getGenericFeedForWebsite("Center for Cognitive Studies", "https://ase.tufts.edu/cogstud/news.html", savedBucket)
  }

  private fun getNativeFeedForWebsite(
    title: String,
    websiteUrl: String,
    bucket: BucketEntity,
    harvestItems: Boolean,
    filter: String = ""
  ) {
    val fetchOptions = GenericFeedFetchOptions(
      websiteUrl
    )
    val feed = feedDiscoveryService.discoverFeeds(corrId, fetchOptions).results.nativeFeeds.first()
    val nativeFeed =
      nativeFeedService.createNativeFeed(corrId, title, "", feed.url, websiteUrl, harvestItems, false, user)
    val importer = ImporterEntity()
    importer.feed = nativeFeed
    importer.bucket = bucket
    importer.autoRelease = false
    importer.owner = user
    importer.filter = filter
    importerDAO.save(importer)
  }

  private fun getGenericFeedForWebsite(
    title: String,
    websiteUrl: String,
    bucket: BucketEntity,
    filter: String = ""
  ) {
    val corrId = ""
    val discovery = feedDiscoveryService.discoverFeeds(corrId, GenericFeedFetchOptions(websiteUrl = websiteUrl))
    val bestRule = discovery.results.genericFeedRules.first()
    log.info("feedUrl ${bestRule.feedUrl}")
    val nativeFeed = nativeFeedService.createNativeFeed(corrId, title, "", bestRule.feedUrl, websiteUrl, harvestSite, false, user)

    val genericFeed = GenericFeedEntity()
    genericFeed.feedSpecification = GenericFeedSpecification(
      selectors = GenericFeedSelectors(
        linkXPath = bestRule.linkXPath,
        extendContext = bestRule.extendContext,
        contextXPath = bestRule.contextXPath,
        dateXPath = bestRule.dateXPath,
      ),
      parserOptions = GenericFeedParserOptions(),
      fetchOptions = GenericFeedFetchOptions(
        websiteUrl = websiteUrl,
        prerender = false,
        prerenderWaitUntil = PuppeteerWaitUntil.load,
        prerenderWithoutMedia = false,
        prerenderScript = ""
      ),
      refineOptions = GenericFeedRefineOptions(),
    )
    genericFeed.websiteUrl = websiteUrl
    genericFeed.nativeFeed = nativeFeed

    genericFeedDAO.save(genericFeed)


    val importer = ImporterEntity()
    importer.feed = nativeFeed
    importer.bucket = bucket
    importer.autoRelease = false
    importer.owner = user
    importer.filter = filter
    importerDAO.save(importer)
  }
}
