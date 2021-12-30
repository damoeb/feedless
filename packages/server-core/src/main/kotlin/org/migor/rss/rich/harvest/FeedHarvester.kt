package org.migor.rss.rich.harvest

import com.rometools.rome.feed.module.DCModule
import com.rometools.rome.feed.synd.SyndContent
import com.rometools.rome.feed.synd.SyndEntry
import org.apache.commons.lang3.StringUtils
import org.migor.rss.rich.database.enums.FeedStatus
import org.migor.rss.rich.database.model.Article
import org.migor.rss.rich.database.model.ArticleRefType
import org.migor.rss.rich.database.model.Feed
import org.migor.rss.rich.database.model.NamespacedTag
import org.migor.rss.rich.database.model.TagNamespace
import org.migor.rss.rich.database.repository.ArticleRepository
import org.migor.rss.rich.database.repository.FeedRepository
import org.migor.rss.rich.harvest.feedparser.FeedContextResolver
import org.migor.rss.rich.harvest.feedparser.NativeFeedResolver
import org.migor.rss.rich.harvest.feedparser.TwitterFeedResolver
import org.migor.rss.rich.service.ArticleService
import org.migor.rss.rich.service.ExporterTargetService
import org.migor.rss.rich.service.FeedService
import org.migor.rss.rich.service.FilterService
import org.migor.rss.rich.service.HttpService
import org.migor.rss.rich.service.ScoreService
import org.migor.rss.rich.util.CryptUtil
import org.migor.rss.rich.util.HtmlUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.util.MimeType
import java.util.*
import java.util.stream.Collectors
import javax.annotation.PostConstruct


@Service
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
    try {
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
    } catch (ex: Exception) {
      ex.printStackTrace()
      log.error("[$corrId] Harvest failed ${ex.message}")
      feedService.updateNextHarvestDateAfterError(corrId, feed, ex)
    } finally {
      this.log.debug("[$corrId] Finished feed ${feed.feedUrl}")
    }
  }

  private fun updateFeedDetails(corrId: String, feedData: FeedData, feed: Feed) {
    log.info("[${corrId}] Updating feed ${feed.id}")
    feed.description = StringUtils.trimToNull(feedData.feed.description)
    feed.title = feedData.feed.title
    feed.author = feedData.feed.author
    feed.tags =
      feedData.feed.categories.map { syndCategory -> NamespacedTag(TagNamespace.INHERITED, syndCategory.name) }.toList()
    feed.lang = lang(feedData.feed.language)
    val dcModule = feedData.feed.getModule("http://purl.org/dc/elements/1.1/") as DCModule?
    if (dcModule != null && feed.lang == null) {
      feed.lang = lang(dcModule.language)
    }
    feed.homePageUrl = feedData.feed.link
//    var ftData = arrayOf(feed.title, feed.description, feed.feedUrl, feed.homePageUrl)
//    if (!feed.homePageUrl.isNullOrEmpty() && feed.status === FeedStatus.unresolved) {
//      try {
//        val response = httpService.httpGet(corrId, feed.homePageUrl!!, 200)
//        val doc = Jsoup.parse(response.responseBody)
//        ftData = ftData.plus(doc.title())
//      } catch (e: HarvestException) {
//        // ignore
//      }
//    }

    feedService.update(feed)
  }

  private fun lang(language: String?): String? {
    val lang = StringUtils.trimToNull(language)
    return if (lang == null || lang.length < 2) {
      null
    } else {
      lang.substring(0, 2)
    }
  }

  private fun handleFeedData(
    corrId: String,
    feed: Feed,
    feedData: List<FeedData>,
    feedContextResolver: FeedContextResolver
  ) {
    if (feedData.isNotEmpty()) {
      val articles = feedContextResolver.mergeFeeds(feedData)
        .asSequence()
        .map { article -> createArticle(article, feed) }
        .filterNotNull()
        .filter { article -> matchesOptionalFeedFilter(corrId, article, feed) }
//        .filter { article -> !existsArticleByUrl(article.url!!) } // todo mag insert or update
        .map { article -> saveOrUpdateArticle(corrId, article, feed) }
        .toList()

      val newArticlesCount = articles.stream()
        .filter { pair: Pair<Boolean, Article> -> pair.first }
        .count()
      if (newArticlesCount > 0) {
        log.info("[$corrId] Updated $newArticlesCount articles for ${feed.feedUrl}")
        feedService.updateUpdatedAt(corrId, feed)
        feedService.applyRetentionStrategy(corrId, feed)
      } else {
        log.debug("[$corrId] Up-to-date ${feed.feedUrl}")
      }

      articles
        .forEach { (_, article) ->
          runCatching {
            exporterTargetService.pushArticleToTargets(
              corrId,
              article.id!!,
              feed.streamId!!,
              ArticleRefType.feed,
              "system",
              article.pubDate,
              emptyList(),
            )
          }.onSuccess { log.info("[$corrId] ${feed.streamId} add article ${article.url}") }
            .onFailure { log.error("[${corrId}] pushArticleToTargets failed ${it.message}") }
        }
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
      Pair(true, article)
    }.also { (isNew, changedArticle) ->
      run {
        val savedArticle = articleService.save(changedArticle)
        if (isNew) {
          this.articleService.triggerContentEnrichment(corrId, changedArticle, feed)
        }
        Pair(isNew, savedArticle)
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

  private fun createArticle(articleRef: Pair<SyndEntry, Article>, feed: Feed): Article? {
    return try {
      val syndEntry = articleRef.first
      val article = articleRef.second
      article.url = Optional.ofNullable(syndEntry.link).orElse(syndEntry.uri)
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

      article.author = getAuthor(syndEntry)
      val tags = syndEntry.categories
        .map { syndCategory -> NamespacedTag(TagNamespace.INHERITED, syndCategory.name) }
        .toMutableSet()
      if (syndEntry.enclosures != null && syndEntry.enclosures.isNotEmpty()) {
        tags.addAll(
          syndEntry.enclosures
            .filterNotNull()
            .map { enclusure ->
              NamespacedTag(
                TagNamespace.CONTENT,
                MimeType(enclusure.type).type.lowercase(Locale.getDefault())
              )
            }
        )
      }
//      todo mag support article.enclosures = JsonUtil.gson.toJson(syndEntry.enclosures)
//      article.putDynamicField("", "enclosures", syndEntry.enclosures)
      article.tags = tags.toList()
      article.commentsFeedUrl = syndEntry.comments
//    todo mag add feedUrl as featured link
//      article.sourceUrl = feed.feedUrl
      article.released = !feed.harvestSite

      article.pubDate = Optional.ofNullable(syndEntry.publishedDate).orElse(Date())
      article.createdAt = Date()
      article
    } catch (e: Exception) {
      null
    }
  }

  private fun getAuthor(syndEntry: SyndEntry) =
    Optional.ofNullable(StringUtils.trimToNull(syndEntry.author)).orElse("unknown")

  private fun extractContent(syndEntry: SyndEntry): Pair<Pair<MimeType, String>?, Pair<MimeType, String>?> {
    val contents = ArrayList<SyndContent>()
    contents.addAll(syndEntry.contents)
    if (syndEntry.description != null) {
      contents.add(syndEntry.description)
    }
    val html = contents.find { syndContent ->
      syndContent.type != null && syndContent.type.lowercase(Locale.getDefault()).endsWith("html")
    }?.let { htmlContent -> Pair(MimeType.valueOf("text/html"), htmlContent.value) }
    val text = if (contents.isNotEmpty()) {
      if (html == null) {
        Pair(MimeType.valueOf("text/plain"), contents.first().value)
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
    if (context.prepareRequest != null) {
      log.info("[$branchedCorrId] Preparing request")
      context.prepareRequest.invoke(request)
    }
    log.info("[$branchedCorrId] GET ${context.feedUrl}")
    val response = httpService.executeRequest(branchedCorrId, request, context.expectedStatusCode)
    return HarvestResponse(context.feedUrl, response)
  }
}
