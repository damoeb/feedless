package org.migor.rss.rich.cron

import com.rometools.rome.feed.module.DCModule
import com.rometools.rome.feed.synd.SyndContent
import com.rometools.rome.feed.synd.SyndEntry
import org.apache.commons.lang3.StringUtils
import org.jsoup.Jsoup
import org.jsoup.safety.Whitelist
import org.migor.rss.rich.database.enums.FeedStatus
import org.migor.rss.rich.database.model.Article
import org.migor.rss.rich.database.model.Feed
import org.migor.rss.rich.database.repository.ArticleRepository
import org.migor.rss.rich.database.repository.FeedRepository
import org.migor.rss.rich.harvest.FeedData
import org.migor.rss.rich.harvest.HarvestException
import org.migor.rss.rich.harvest.HarvestResponse
import org.migor.rss.rich.harvest.HarvestUrl
import org.migor.rss.rich.harvest.feedparser.*
import org.migor.rss.rich.harvest.score.ScoreService
import org.migor.rss.rich.service.ArticleService
import org.migor.rss.rich.service.FeedService
import org.migor.rss.rich.service.StreamService
import org.migor.rss.rich.util.FeedUtil
import org.migor.rss.rich.util.HtmlUtil
import org.migor.rss.rich.util.HttpUtil
import org.migor.rss.rich.util.JsonUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.net.ConnectException
import java.util.*
import java.util.stream.Collectors
import javax.annotation.PostConstruct


@Service
class FillFeedCron internal constructor() {

  private val log = LoggerFactory.getLogger(FillFeedCron::class.simpleName)

  @Autowired
  lateinit var scoreService: ScoreService

  @Autowired
  lateinit var streamService: StreamService

  @Autowired
  lateinit var articleService: ArticleService

  @Autowired
  lateinit var articleRepository: ArticleRepository

  @Autowired
  lateinit var feedRepository: FeedRepository

  @Autowired
  lateinit var feedService: FeedService

  private val contentStrategies = arrayOf(
    XmlFeedParser(),
    JsonFeedParser(),
    NullFeedParser()
  )

  private lateinit var feedResolvers: Array<FeedSourceResolver>
//  private lateinit var articlePostProcessors: Array<EntryTransform>

  @PostConstruct
  fun onInit() {
//    val twitterSupport = TwitterFeedSupport(propertyService)
    feedResolvers = arrayOf(
//      twitterSupport,
      NativeFeedResolver()
    )

//    articlePostProcessors = arrayOf(
//      twitterSupport,
//      BaseTransform()
//    )
  }

  @Scheduled(fixedDelay = 4567)
  fun fetchFeeds() {
    val byHarvestAt = PageRequest.of(0, 10, Sort.by(Sort.Order.asc("nextHarvestAt")))
    val pendingSources = feedRepository.findAllByNextHarvestAtIsBeforeAndStatusEquals(Date(), arrayOf(FeedStatus.expired, FeedStatus.stopped), byHarvestAt)

    pendingSources
      .forEach { feed: Feed ->
        try {
          val responses = fetchFeed(feed)
          val feedData = convertToFeeds(responses).filterNotNull()
          if (feedData.isEmpty()) {
            throw RuntimeException("No feeds extracted")
          } else {
            handleFeedData(feed, feedData)
          }

          if (FeedStatus.ok != feed.status) {
            this.log.info("Feed ${feed.feedUrl} status-change ${feed.status} -> ok")
            feedService.redeemStatus(feed)
          }
        } catch (ex: Exception) {
          log.error("Harvest failed source ${feed.id} (${feed.feedUrl}), ${ex.message}")
//          if (log.isDebugEnabled) {
//            e.printStackTrace()
//          }
          feedService.updateNextHarvestDateAfterError(feed, ex)
        } finally {
          this.log.info("Finished feed ${feed.feedUrl}")
        }
      }
  }

  fun updateFeedDetails(feedData: FeedData, feed: Feed) {
    feed.description = StringUtils.trimToNull(feedData.feed.description)
    feed.title = feedData.feed.title
    feed.tags = feedData.feed.categories.map { syndCategory -> syndCategory.name }.toTypedArray()
    feed.lang = lang(feedData.feed.language)
    val dcModule = feedData.feed.getModule("http://purl.org/dc/elements/1.1/") as DCModule?
    if (dcModule != null && feed.lang == null) {
      feed.lang = lang(dcModule.language)
    }
    feed.homePageUrl = feedData.feed.link
    var ftData = arrayOf(feed.title, feed.description, feed.feedUrl, feed.homePageUrl)

    if (!feed.homePageUrl.isNullOrEmpty() && feed.status === FeedStatus.unresolved) {
      try {
        val response = fetchUrl(HarvestUrl(url = feed.homePageUrl!!))
        val doc = Jsoup.parse(response.response.responseBody)
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

//  private fun resolveArticlePostProcessor(feed: Feed): EntryTransform {
//    return articlePostProcessors.first { postProcessor -> postProcessor.canHandle(feed) }
//  }

  private fun handleFeedData(feed: Feed, feedData: List<FeedData>) {
    if (feedData.isNotEmpty()) {
      updateFeedDetails(feedData.first(), feed)

//      val articlePostProcessor = resolveArticlePostProcessor(feed)
      val articles = feedData.first().feed.entries
        .map { syndEntry -> createArticle(syndEntry, feed) }
        .filter { article -> !existsArticleByUrl(article.url!!) }
//        .map { article -> articlePostProcessor.applyTransform(feed, article.first, article.second, feedData) }
        .map { article -> updateArticle(article) }

      val newArticlesCount = articles.stream().filter { pair: Pair<Boolean, Article>? -> pair!!.first }.count()
      if (newArticlesCount > 0) {
        log.info("Updating $newArticlesCount articles for ${feed.feedUrl}")
        feedService.updateUpdatedAt(feed)
      } else {
        log.info("Up-to-date ${feed.feedUrl}")
      }

      articles.map { pair: Pair<Boolean, Article> -> pair.second }
        .forEach { article: Article ->
          streamService.addArticleToFeed(article, feed.streamId!!, "system", emptyArray())
        }
      feedService.updateNextHarvestDate(feed, newArticlesCount > 0)
    }
  }

  private fun existsArticleByUrl(url: String): Boolean {
    return articleRepository.existsByUrl(url)
  }

  private fun updateArticle(article: Article): Pair<Boolean, Article> {
    val optionalEntry = articleRepository.findByUrl(article.url!!)
    return if (optionalEntry.isPresent) {
      Pair(false, updateArticleProperties(optionalEntry.get(), article))
    } else {
      try {
        val readability = articleService.getReadability(article.url!!)
        log.debug("Fetched readability for ${article.url}")
        article.readability = readability
        article.hasReadability = true
      } catch (e: Exception) {
        log.error("Failed to fetch Readability ${article.url}: ${e.message}")
        article.hasReadability = false
      }
      Pair(true, article)
    }
  }

  private fun updateArticleProperties(existingArticle: Article, newArticle: Article): Article {
    existingArticle.title = newArticle.title
    existingArticle.content = newArticle.content
    existingArticle.contentHtml = newArticle.contentHtml
    existingArticle.tags = newArticle.tags
    return existingArticle
  }

  private fun createArticle(syndEntry: SyndEntry, feed: Feed): Article {
    val article = Article()
    article.url = syndEntry.link
    article.title = syndEntry.title
    val (text, html) = extractContent(syndEntry)
    article.content = text
    if (StringUtils.isNoneBlank(html)) {
      article.contentHtml = cleanHtml(html!!)
    }
    article.author = getAuthor(syndEntry)
    article.tags = syndEntry.categories.map { syndCategory -> syndCategory.name }.toTypedArray()
    article.enclosures = JsonUtil.gson.toJson(syndEntry.enclosures)
    article.commentsFeedUrl = syndEntry.comments
    article.sourceUrl = feed.feedUrl

    article.pubDate = Optional.ofNullable(syndEntry.publishedDate).orElse(Date())
    article.createdAt = Date()
    return article
  }

  private fun cleanHtml(html: String): String {
    return Jsoup.clean(html, Whitelist.basicWithImages())
  }

  private fun getAuthor(syndEntry: SyndEntry) =
    Optional.ofNullable(StringUtils.trimToNull(syndEntry.author)).orElse("unknown")

  private fun extractContent(syndEntry: SyndEntry): Pair<String?, String?> {
    val contents = ArrayList<SyndContent>()
    contents.addAll(syndEntry.contents)
    if (syndEntry.description != null) {
      contents.add(syndEntry.description)
    }
    val html = contents.find { syndContent -> syndContent.type != null && syndContent.type.toLowerCase().endsWith("html") }?.value
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

  private fun convertToFeeds(responses: List<HarvestResponse>): List<FeedData?> {
    return responses.map { response ->
      try {
        contentStrategies.first { contentStrategy -> contentStrategy.canProcess(FeedUtil.detectFeedTypeForResponse(response.response)) }.process(response)
      } catch (e: Exception) {
        log.error("Failed to convert feed ${e.message}")
        null
      }
    }
  }

  private fun fetchFeed(source: Feed): List<HarvestResponse> {
    val feedResolver = resolveFeedResolver(source)
    return feedResolver.feedUrls(source).stream()
      .map { url -> fetchUrl(url) }
      .collect(Collectors.toList())

  }

  private fun resolveFeedResolver(source: Feed): FeedSourceResolver {
    return feedResolvers.first { feedResolver -> feedResolver.canHandle(source) }
  }

  private fun fetchUrl(url: HarvestUrl): HarvestResponse {
    log.info("Fetching $url")
    val request = HttpUtil.client.prepareGet(url.url).execute()

    val response = try {
      request.get()
    } catch (e: ConnectException) {
      throw HarvestException("Cannot connect to ${url.url} cause ${e.message}")
    }
    if (response.statusCode != 200) {
      throw HarvestException("Expected 200 received ${response.statusCode}")
    }
    return HarvestResponse(url, response)
  }

}

