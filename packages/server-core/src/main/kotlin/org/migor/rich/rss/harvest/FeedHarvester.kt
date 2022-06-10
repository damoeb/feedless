package org.migor.rich.rss.harvest

import org.apache.commons.lang3.StringUtils
import org.migor.rich.rss.api.dto.RichArticle
import org.migor.rich.rss.api.dto.RichtFeed
import org.migor.rich.rss.database.enums.FeedStatus
import org.migor.rich.rss.database.model.Article
import org.migor.rich.rss.database.model.ArticleRefType
import org.migor.rich.rss.database.model.Feed
import org.migor.rich.rss.database.model.NamespacedTag
import org.migor.rich.rss.database.model.TagNamespace
import org.migor.rich.rss.database.repository.ArticleRepository
import org.migor.rich.rss.database.repository.FeedRepository
import org.migor.rich.rss.harvest.feedparser.FeedContextResolver
import org.migor.rich.rss.harvest.feedparser.NativeFeedResolver
import org.migor.rich.rss.harvest.feedparser.TwitterFeedResolver
import org.migor.rich.rss.service.ArticleService
import org.migor.rich.rss.service.ExporterTargetService
import org.migor.rich.rss.service.FeedService
import org.migor.rich.rss.service.FilterService
import org.migor.rich.rss.service.HttpService
import org.migor.rich.rss.service.PropertyService
import org.migor.rich.rss.service.ScoreService
import org.migor.rich.rss.util.CryptUtil
import org.migor.rich.rss.util.HtmlUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.util.MimeType
import java.util.*
import java.util.stream.Collectors
import javax.annotation.PostConstruct


@Service
@Profile("stateful")
class FeedHarvester internal constructor() {

  private val log = LoggerFactory.getLogger(FeedHarvester::class.simpleName)

  @Autowired
  lateinit var articleService: ArticleService

  @Autowired
  lateinit var scoreService: ScoreService

  @Autowired
  lateinit var exporterTargetService: ExporterTargetService

  @Autowired
  lateinit var articleRepository: ArticleRepository

  @Autowired
  lateinit var feedRepository: FeedRepository

  @Autowired
  lateinit var feedService: FeedService

  @Autowired
  lateinit var propertyService: PropertyService

  @Autowired
  lateinit var filterService: FilterService

  @Autowired
  lateinit var httpService: HttpService

  @Autowired
  lateinit var nativeFeedResolver: NativeFeedResolver

  @Autowired
  lateinit var twitterFeedResolver: TwitterFeedResolver

  private lateinit var feedContextResolvers: Array<FeedContextResolver>

  @PostConstruct
  fun onInit() {
    feedContextResolvers = arrayOf(
      nativeFeedResolver,
      twitterFeedResolver
    )

    feedContextResolvers.sortByDescending { feedUrlResolver -> feedUrlResolver.priority() }
    log.info(
      "Using feedUrlResolvers ${
        feedContextResolvers.map { feedUrlResolver -> "$feedUrlResolver priority: ${feedUrlResolver.priority()}" }
          .joinToString(", ")
      }"
    )
  }

  fun harvestFeed(corrId: String, feed: Feed) {
    runCatching {
      this.log.info("[$corrId] Harvesting feed ${feed.id} (${feed.feedUrl})")
      val feedContextResolver = findFeedContextResolver(feed)
      val feedData =
        fetchFeed(corrId, feed, feedContextResolver).map { response -> feedService.parseFeed(corrId, response) }
      if (feedData.isEmpty()) {
        throw RuntimeException("[$corrId] No feeds extracted")
      } else {
        updateFeedDetails(corrId, feedData.first(), feed)
        handleFeedData(corrId, feed, feedData, feedContextResolver)
      }

      if (FeedStatus.ok != feed.status) {
        this.log.debug("[$corrId] status-change for Feed ${feed.feedUrl}: ${feed.status} -> ok")
        feedService.redeemStatus(feed)
      }
      feed.failedAttemptCount = 0

    }.onFailure { ex ->
      run {
        log.error("[$corrId] Harvest failed ${ex.message}")
        feedService.updateNextHarvestDateAfterError(corrId, feed, ex)
      }
    }
  }

  private fun updateFeedDetails(corrId: String, syndFeed: RichtFeed, feed: Feed) {
    log.debug("[${corrId}] Updating feed ${feed.id}")
    var changed = false
    val title = StringUtils.trimToNull(syndFeed.title)
    if (feed.title != title) {
      log.info("[${corrId}] title ${feed.title} -> $title")
      feed.title = title
      changed = true
    }
    val author = StringUtils.trimToNull(syndFeed.author)
    if (feed.author != author) {
      log.info("[${corrId}] author ${feed.author} -> $author")
      feed.author = author
      changed = true
    }
    val description = StringUtils.trimToNull(syndFeed.description)
    if (feed.description != description) {
      log.info("[${corrId}] description ${feed.description} -> $description")
      feed.description = StringUtils.trimToNull(description)
      changed = true
    }
    val homePageUrl = StringUtils.trimToNull(syndFeed.home_page_url)
    if (feed.homePageUrl != homePageUrl) {
      log.info("[${corrId}] homePageUrl ${feed.homePageUrl} -> $homePageUrl")
      feed.homePageUrl = homePageUrl
      changed = true
    }
    feed.tags =
      syndFeed.tags?.map { syndCategory -> NamespacedTag(TagNamespace.INHERITED, syndCategory) }

    if (changed) {
      feedService.update(feed)
      log.debug("[${corrId}] Updated feed ${feed.id}")
    }
  }

  private fun handleFeedData(
    corrId: String,
    feed: Feed,
    syndFeeds: List<RichtFeed>,
    feedContextResolver: FeedContextResolver
  ) {
    if (syndFeeds.isNotEmpty()) {
      val articles = feedContextResolver.mergeFeeds(syndFeeds)
        .asSequence()
        .map { article -> createArticle(article, feed) }
        .filterNotNull()
        .filter { article -> matchesOptionalFeedFilter(corrId, article, feed) }
        .map { article -> saveOrUpdateArticle(corrId, article, feed) }
        .toList()

      val newArticlesCount = articles.stream()
        .filter { pair: Pair<Boolean, Article> -> pair.first }
        .count()
      if (newArticlesCount > 0) {
        log.info("[$corrId] Appended $newArticlesCount articles")
        feedService.updateUpdatedAt(corrId, feed)
        feedService.applyRetentionStrategy(corrId, feed)
      } else {
        log.debug("[$corrId] Up-to-date ${feed.feedUrl}")
      }

      // todo mag forward all articles at once
      articles
        .filter { (isNew, _) -> isNew }
        .forEach { (_, article) ->
          runCatching {
            exporterTargetService.pushArticleToTargets(
              corrId,
              article,
              feed.streamId!!,
              ArticleRefType.feed,
              "system", // todo mag fix ownerId
              article.pubDate,
              targets = emptyList(),
            )
          }.onFailure { log.error("[${corrId}] pushArticleToTargets failed: ${it.message}") }
        }
      log.info("Updated feed ${propertyService.host}/feed:${feed.id}")
      feedService.updateNextHarvestDate(corrId, feed, newArticlesCount > 0)
      if (newArticlesCount > 0) {
        this.feedRepository.setLastUpdatedAt(feed.id!!, Date())
      }
    } else {
      feedService.updateNextHarvestDate(corrId, feed, false)
    }
  }

  private fun matchesOptionalFeedFilter(corrId: String, article: Article, feed: Feed): Boolean {
    return Optional.ofNullable(feed.filter)
      .map {
        filterService.filter(
          corrId,
          article,
          it
        )
      }
      .orElse(true)
  }

  private fun saveOrUpdateArticle(corrId: String, article: Article, feed: Feed): Pair<Boolean, Article> {
    val optionalEntry = Optional.ofNullable(articleRepository.findByUrl(article.url!!))
    return if (optionalEntry.isPresent) {
      Pair(false, updateArticleProperties(optionalEntry.get(), article))
    } else {
      Pair(true, articleService.triggerContentEnrichment(corrId, article, feed))
    }.also { (isNew, changedArticle) ->
      run {
        Pair(isNew, articleService.save(changedArticle))
      }
    }
  }

  private fun updateArticleProperties(existingArticle: Article, newArticle: Article): Article {
    val changedTitle = existingArticle.title.equals(newArticle.title)
    if (changedTitle) {
      existingArticle.title = newArticle.title
    }
    val changedContent = existingArticle.contentRaw == newArticle.contentRaw
    if (changedContent) {
      existingArticle.contentRaw = newArticle.contentRaw
    }
    val changedContentHtml = existingArticle.contentText.equals(newArticle.contentText)
    if (changedContentHtml) {
      existingArticle.contentText = newArticle.contentText
    }

    val allTags = HashSet<NamespacedTag>()
    newArticle.tags?.let { tags -> allTags.addAll(tags) }
    existingArticle.tags?.let { tags -> allTags.addAll(tags) }
    existingArticle.tags = allTags.toList()
    return existingArticle
  }

  private fun createArticle(articleRef: Pair<RichArticle, Article>, feed: Feed): Article? {
    return try {
      val syndEntry = articleRef.first
      val article = articleRef.second
      article.url = syndEntry.url
      article.title = syndEntry.title

      val (text, html) = extractContent(syndEntry)
      if (StringUtils.isBlank(article.contentRaw)) {
        html?.let { t ->
          run {
            article.contentRaw = t.second
            article.contentRawMime = t.first.toString()
          }
        }
      }
      text?.let { t ->
        run {
          article.contentText = HtmlUtil.html2text(t.second)
        }
      }

      article.author = syndEntry.author
//      val tags = syndEntry.tags.toMutableSet()
//      if (syndEntry.enclosures != null && syndEntry.enclosures.isNotEmpty()) {
//        tags.addAll(
//          syndEntry.enclosures
//            .map { enclusure ->
//              NamespacedTag(
//                TagNamespace.CONTENT,
//                MimeType(enclusure.type).type.lowercase(Locale.getDefault())
//              )
//            }
//        )
//      }
//      todo mag support article.enclosures = JsonUtil.gson.toJson(syndEntry.enclosures)
//      article.putDynamicField("", "enclosures", syndEntry.enclosures)
//      article.tags = tags.toList()
//      article.commentsFeedUrl = syndEntry.comments
//    todo mag add feedUrl as featured link
//      article.sourceUrl = feed.feedUrl
      article.released = !feed.harvestSite

      article.pubDate = Optional.ofNullable(syndEntry.publishedAt).orElse(Date())
      article.createdAt = Date()
      article
    } catch (e: Exception) {
      null
    }
  }

  private fun extractContent(syndEntry: RichArticle): Pair<Pair<MimeType, String>?, Pair<MimeType, String>?> {
    val contents = ArrayList<Pair<String,String>>()
    contents.add(Pair("text/plain", syndEntry.contentText))
    syndEntry.contentRaw?.let {
      contents.add(Pair(syndEntry.contentRawMime!!, it))
    }
    val html = contents.find { (mime) ->
      mime.lowercase(Locale.getDefault()).endsWith("html")
    }?.let { htmlContent -> Pair(MimeType.valueOf("text/html"), htmlContent.second) }
    val text = if (contents.isNotEmpty()) {
      if (html == null) {
        Pair(MimeType.valueOf("text/plain"), contents.first().second)
      } else {
        Pair(MimeType.valueOf("text/plain"), HtmlUtil.html2text(html.second))
      }
    } else {
      null
    }
    return Pair(text, html)
  }

  private fun fetchFeed(corrId: String, feed: Feed, feedContextResolver: FeedContextResolver): List<HarvestResponse> {
    return Optional.ofNullable(feedContextResolver)
      .orElseThrow { throw RuntimeException("No feedContextResolver found for feed ${feed.feedUrl}") }
      .getHarvestContexts(corrId, feed)
      .stream()
      .map { context -> fetchFeedUrl(corrId, context) }
      .collect(Collectors.toList())
  }

  private fun findFeedContextResolver(feed: Feed): FeedContextResolver {
    return feedContextResolvers.first { feedResolver -> feedResolver.canHarvest(feed) }
  }

  private fun fetchFeedUrl(corrId: String, context: HarvestContext): HarvestResponse {
    val branchedCorrId = CryptUtil.newCorrId(parentCorrId = corrId)
    val request = httpService.prepareGet(context.feedUrl)

    context.feed.feedUrlAuthHeader?.let {
      log.info("[$branchedCorrId] Using auth header")
      request.addHeader("Authorization", it)
    }

    if (context.prepareRequest != null) {
      log.info("[$branchedCorrId] Preparing request")
      context.prepareRequest.invoke(request)
    }
    log.info("[$branchedCorrId] GET ${context.feedUrl}")
    val response = httpService.executeRequest(branchedCorrId, request, context.expectedStatusCode)
    return HarvestResponse(context.feedUrl, response)
  }
}
