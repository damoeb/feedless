package org.migor.rss.rich.harvest

import com.rometools.rome.feed.module.DCModule
import com.rometools.rome.feed.synd.SyndContent
import com.rometools.rome.feed.synd.SyndEntry
import org.apache.commons.lang3.StringUtils
import org.jsoup.Jsoup
import org.migor.rss.rich.database.enums.FeedStatus
import org.migor.rss.rich.database.model.Article
import org.migor.rss.rich.database.model.Feed
import org.migor.rss.rich.database.model.NamespacedTag
import org.migor.rss.rich.database.model.TagNamespace
import org.migor.rss.rich.database.repository.ArticleRepository
import org.migor.rss.rich.database.repository.FeedRepository
import org.migor.rss.rich.harvest.feedparser.FeedContextResolver
import org.migor.rss.rich.harvest.feedparser.NativeFeedResolver
import org.migor.rss.rich.service.FeedService
import org.migor.rss.rich.service.HttpService
import org.migor.rss.rich.service.ArticleService
import org.migor.rss.rich.service.StreamService
import org.migor.rss.rich.util.HtmlUtil
import org.migor.rss.rich.util.JsonUtil
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
  lateinit var streamService: StreamService

  @Autowired
  lateinit var articleRepository: ArticleRepository

  @Autowired
  lateinit var feedRepository: FeedRepository

  @Autowired
  lateinit var feedService: FeedService

  @Autowired
  lateinit var httpService: HttpService

  private lateinit var feedContextResolvers: Array<FeedContextResolver>

  @PostConstruct
  fun onInit() {
    feedContextResolvers = arrayOf(
      NativeFeedResolver()
    )

    feedContextResolvers.sortByDescending { feedUrlResolver -> feedUrlResolver.priority() }
    log.info("Using feedUrlResolvers ${feedContextResolvers.map { feedUrlResolver -> "${feedUrlResolver.toString()} priority: ${feedUrlResolver.priority()}" }.joinToString(", ")}")
  }

  fun harvestFeed(cid: String, feed: Feed) {
    try {
      this.log.info("[${cid}] Harvesting feed ${feed.id} (${feed.feedUrl})")
      val feedData = fetchFeed(cid, feed).map { response -> feedService.parseFeed(cid, response) }
      if (feedData.isEmpty()) {
        throw RuntimeException("[${cid}] No feeds extracted")
      } else {
        handleFeedData(cid, feed, feedData)
      }

      if (FeedStatus.ok != feed.status) {
        this.log.debug("[${cid}] status-change for Feed ${feed.feedUrl}: ${feed.status} -> ok")
        feedService.redeemStatus(feed)
      }
    } catch (ex: Exception) {
      log.error("[${cid}] Harvest failed ${ex.message}")
      if (log.isInfoEnabled) {
//            ex.printStackTrace()
      }
      feedService.updateNextHarvestDateAfterError(cid, feed, ex)
    } finally {
      this.log.debug("[${cid}] Finished feed ${feed.feedUrl}")
    }
  }

  private fun updateFeedDetails(cid: String, feedData: FeedData, feed: Feed) {
    feed.description = StringUtils.trimToNull(feedData.feed.description)
    feed.title = feedData.feed.title
    feed.tags =
      feedData.feed.categories.map { syndCategory -> NamespacedTag(TagNamespace.NONE, syndCategory.name) }.toList()
    feed.lang = lang(feedData.feed.language)
    val dcModule = feedData.feed.getModule("http://purl.org/dc/elements/1.1/") as DCModule?
    if (dcModule != null && feed.lang == null) {
      feed.lang = lang(dcModule.language)
    }
    feed.homePageUrl = feedData.feed.link
    var ftData = arrayOf(feed.title, feed.description, feed.feedUrl, feed.homePageUrl)

    if (!feed.homePageUrl.isNullOrEmpty() && feed.status === FeedStatus.unresolved) {
      try {
        val response = httpService.httpGet(feed.homePageUrl!!)
        if (response.statusCode != 200) {
          throw HarvestException("Expected 200 received ${response.statusCode}")
        }
        val doc = Jsoup.parse(response.responseBody)
        ftData = ftData.plus(doc.title())
      } catch (e: HarvestException) {
        // ignore
      }
    }
    feed.fulltext = ftData
      .filter { value -> StringUtils.isNotBlank(value) }.joinToString { " " }

    feedRepository.save(feed)
  }

  private fun lang(language: String?): String? {
    val lang = StringUtils.trimToNull(language)
    return if (lang == null || lang.length < 2) {
      null
    } else {
      lang.substring(0, 2)
    }
  }

  private fun handleFeedData(cid: String, feed: Feed, feedData: List<FeedData>) {
    if (feedData.isNotEmpty()) {
      updateFeedDetails(cid, feedData.first(), feed)

      val articles = feedData.first().feed.entries
        .asSequence()
        .filterNotNull()
        .map { syndEntry -> createArticle(syndEntry, feed) }
        .filterNotNull()
        .filter { article -> !existsArticleByUrl(article.url!!) }
        .map { article -> enrichArticle(cid, article) }
        .toList()

      val newArticlesCount = articles.stream().filter { pair: Pair<Boolean, Article>? -> pair!!.first }.count()
      if (newArticlesCount > 0) {
        log.info("[${cid}] Updating $newArticlesCount articles for ${feed.feedUrl}")
        feedService.updateUpdatedAt(cid, feed)
      } else {
        log.debug("[${cid}] Up-to-date ${feed.feedUrl}")
      }

      articles.map { pair: Pair<Boolean, Article> -> pair.second }
        .forEach { article: Article ->
          run {
            this.log.debug("[${cid}] Adding article ${article.url} to feed ${feed.title}")
            streamService.addArticleToStream(cid, article, feed.streamId!!, "system", emptyList())
          }
        }
      feedService.updateNextHarvestDate(cid, feed, newArticlesCount > 0)
      if (newArticlesCount > 0) {
        this.feedRepository.setLastUpdatedAt(feed.id!!, Date())
      }
    } else {
      feedService.updateNextHarvestDate(cid, feed, false)
    }
  }

  private fun existsArticleByUrl(url: String): Boolean {
    return articleRepository.existsByUrl(url)
  }

  private fun enrichArticle(cid: String, article: Article): Pair<Boolean, Article> {
    val optionalEntry = articleRepository.findByUrl(article.url!!)
    return if (optionalEntry.isPresent) {
      val (updatedArticle, changed) = updateArticleProperties(optionalEntry.get(), article)
      this.articleService.triggerContentEnrichment(cid, updatedArticle)
      Pair(false, updatedArticle)
    } else {
      this.articleService.triggerContentEnrichment(cid, article)
      Pair(true, article)
    }
  }

  private fun updateArticleProperties(existingArticle: Article, newArticle: Article): Pair<Article, Boolean> {
    val changedTitle = existingArticle.title.equals(newArticle.title)
    if (changedTitle) {
      existingArticle.title = newArticle.title
    }
    val changedContent = existingArticle.content.equals(newArticle.content)
    if (changedContent) {
      existingArticle.content = newArticle.content
    }
    val changedContentHtml = existingArticle.contentHtml.equals(newArticle.contentHtml)
    if (changedContentHtml) {
      existingArticle.contentHtml = newArticle.contentHtml
    }

    val allTags = HashSet<NamespacedTag>()
    newArticle.tags?.let { tags -> allTags.addAll(tags) }
    existingArticle.tags?.let { tags -> allTags.addAll(tags) }
    existingArticle.tags = allTags.toList()
    return Pair(existingArticle, changedTitle || changedContent || changedContentHtml)
  }

  private fun createArticle(syndEntry: SyndEntry, feed: Feed): Article? {
    return try {
      val article = Article()
      article.url = syndEntry.link
      article.title = syndEntry.title
      val (text, html) = extractContent(syndEntry)
      article.content = text!!
      article.contentHtml = HtmlUtil.cleanHtml(html)
      article.author = getAuthor(syndEntry)
      val tags = syndEntry.categories
        .map { syndCategory -> NamespacedTag(TagNamespace.NONE, syndCategory.name) }
        .toMutableSet()
      if (syndEntry.enclosures != null && syndEntry.enclosures.isNotEmpty()) {
        tags.addAll(syndEntry.enclosures
          .filterNotNull()
          .map { enclusure -> NamespacedTag(TagNamespace.CONTENT, MimeType(enclusure.type).type.toLowerCase()) })
      }
      article.enclosures = JsonUtil.gson.toJson(syndEntry.enclosures)
      article.tags = tags.toList()
      article.commentsFeedUrl = syndEntry.comments
      article.sourceUrl = feed.feedUrl

      article.pubDate = Optional.ofNullable(syndEntry.publishedDate).orElse(Date())
      article.createdAt = Date()
      article
    } catch (e: Exception) {
      null
    }
  }

  private fun getAuthor(syndEntry: SyndEntry) =
    Optional.ofNullable(StringUtils.trimToNull(syndEntry.author)).orElse("unknown")

  private fun extractContent(syndEntry: SyndEntry): Pair<String?, String?> {
    val contents = ArrayList<SyndContent>()
    contents.addAll(syndEntry.contents)
    if (syndEntry.description != null) {
      contents.add(syndEntry.description)
    }
    val html = contents.find { syndContent ->
      syndContent.type != null && syndContent.type.toLowerCase().endsWith("html")
    }?.value
    val text = if (contents.isNotEmpty()) {
      if (html == null) {
        contents.first().value
      } else {
        HtmlUtil.html2text(html)
      }
    } else {
      null
    }
    return Pair(text, html)
  }

  private fun fetchFeed(cid: String, feed: Feed): List<HarvestResponse> {
    val feedContextResolver = findFeedContextResolver(feed)
    return Optional.ofNullable(feedContextResolver)
      .orElseThrow { throw RuntimeException("No feedContextResolver found for feed ${feed.feedUrl}") }
      .getHarvestContexts(feed)
      .stream()
      .map { context -> fetchFeedUrl(cid, context) }
      .collect(Collectors.toList())

  }

  private fun findFeedContextResolver(feed: Feed): FeedContextResolver? {
    return feedContextResolvers.first { feedResolver -> feedResolver.canHarvest(feed) }
  }

  private fun fetchFeedUrl(cid: String, context: HarvestContext): HarvestResponse {
    val request = httpService.prepareGet(context.feedUrl)
    if (context.prepareRequest != null) {
      log.info("[${cid}] Preparing request")
      context.prepareRequest?.invoke(request)
    }
    log.info("[${cid}] Fetching ${context.feedUrl}")
    val response = httpService.executeRequest(request)

    if (response.statusCode != context.expectedStatusCode) {
      throw HarvestException("Expected ${context.expectedStatusCode} received ${response.statusCode}")
    }
    return HarvestResponse(context.feedUrl, response)
  }

}

