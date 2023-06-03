package org.migor.feedless.web

import org.apache.commons.lang3.StringUtils
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import org.migor.feedless.api.dto.RichArticle
import org.migor.feedless.api.dto.RichFeed
import org.migor.feedless.service.FeedService.Companion.absUrl
import org.migor.feedless.service.FilterService
import org.migor.feedless.service.HttpService
import org.migor.feedless.service.PropertyService
import org.migor.feedless.service.PuppeteerService
import org.migor.feedless.util.FeedUtil
import org.migor.feedless.util.HtmlUtil.parseHtml
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import us.codecraft.xsoup.Xsoup
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.*

@Service
class WebToFeedService {

  private val log = LoggerFactory.getLogger(WebToFeedService::class.simpleName)

  @Autowired
  lateinit var httpService: HttpService

  @Autowired
  lateinit var propertyService: PropertyService

  @Autowired
  lateinit var webToFeedTransformer: WebToFeedTransformer

  @Autowired
  lateinit var filterService: FilterService

  @Autowired
  lateinit var puppeteerService: PuppeteerService

  @Value("\${app.apiGatewayUrl}")
  lateinit var apiGatewayUrl: String

  fun applyRule(
    corrId: String,
    feedUrl: String,
    selectors: GenericFeedSelectors,
    fetchOptions: FetchOptions,
    parserOptions: GenericFeedParserOptions,
    refineOptions: GenericFeedRefineOptions,
  ): RichFeed {
    val url = fetchOptions.websiteUrl
    log.info("[${corrId}] applyRule")

    validateVersion(parserOptions.version)
    httpService.guardedHttpResource(
      corrId,
      url,
      200,
      listOf("text/", "application/xml", "application/json", "application/rss", "application/atom", "application/rdf")
    )

    val markup = if (fetchOptions.prerender) {
      val puppeteerResponse =
        puppeteerService.prerender(corrId, fetchOptions)
          .blockOptional()
          .orElseThrow{ IllegalArgumentException("empty agent response") }
      puppeteerResponse.dataAscii!!
    } else {
      val response = httpService.httpGetCaching(corrId, url, 200)
      String(response.responseBody, Charsets.UTF_8)
    }

    return applyRule(corrId, url, feedUrl, markup, selectors, parserOptions, refineOptions)
  }

  fun applyRule(
    corrId: String,
    url: String,
    feedUrl: String,
    markup: String,
    selectors: GenericFeedSelectors,
    parserOptions: GenericFeedParserOptions,
    refineOptions: GenericFeedRefineOptions,
  ): RichFeed {
    log.debug("[${corrId}] applyRule")

    validateVersion(parserOptions.version)
    val doc = parseHtml(markup, url)
    val items = webToFeedTransformer.getArticlesBySelectors(corrId, selectors, doc, URL(url))
      .asSequence()
      .distinctBy { it.url }
      .filter { filterService.matches(corrId, it, refineOptions.filter) }
      .toList()

    return createFeed(url, doc.title(), items, feedUrl)
  }

  private fun getNextUrlUsingPagination(doc: Document, paginationXPath: String?, url: String): String? {
    return if (StringUtils.isBlank(paginationXPath)) {
      null
    } else {
      Optional.ofNullable(Xsoup.compile(paginationXPath).evaluate(doc).elements.firstOrNull())
        .map { paginationContext ->
          run {
            // cleanup
            paginationContext.childNodes().filterIsInstance<TextNode>()
              .filter { it.text().trim().replace(Regex("[^a-zA-Z<>0-9]+"), "").isEmpty() }
              .forEach { it.remove() }

            paginationContext.childNodes().filterIsInstance<TextNode>().forEach { it.replaceWith(toSpan(it)) }

            // detect anomaly
            val links = paginationContext.select("a[href]").map {
              run {
                var element = it
                while (element.parent() != paginationContext) {
                  element = element.parent()
                }
                Pair(element, it.attr("href"))
              }
            }

            if (links.any { link -> link.second == url }) {
              links.dropWhile { child -> links.any { it.second == url } }
              if (links.isEmpty()) {
                null
              } else {
                absUrl(url, links.first().second)
              }
            } else {
              val children = paginationContext.children()
              val relativeNextUrl =
                children.dropWhile { child -> links.any { it.first == it } }
                  .first { child -> links.any { it.first == child } }
                  .select("a[href]").attr("href")

              absUrl(url, relativeNextUrl)
            }
          }
        }.orElse(null)
    }
  }

  private fun toSpan(it: TextNode): Element {
    val span = Element("span")
    span.text(it.text())
    return span
  }

  private fun createFeed(
    homePageUrl: String,
    title: String,
    items: List<RichArticle>,
    feedUrl: String,
    nextPage: String? = null
  ): RichFeed {
    val richFeed = RichFeed()
    richFeed.id = feedUrl
    richFeed.title = title
    richFeed.websiteUrl = homePageUrl
    richFeed.publishedAt = Date()
    richFeed.items = items
    richFeed.feedUrl = feedUrl
    richFeed.nextUrl = nextPage
    richFeed.editUrl = "/wizard?feedUrl=${URLEncoder.encode(feedUrl, StandardCharsets.UTF_8)}"
    return richFeed
  }

  fun createMaintenanceFeed(corrId: String, homePageUrl: String, feedUrl: String, article: RichArticle): RichFeed {
    log.info("[${corrId}] falling back to maintenance feed")
    return createFeed(
      homePageUrl,
      "Maintenance",
      listOf(article),
      feedUrl
    )
  }

  private fun encode(param: String): String = URLEncoder.encode(
    param,
    StandardCharsets.UTF_8
  )

  fun createMaintenanceArticle(e: Throwable, url: String): RichArticle {
    // distinguish if an exception will be permanent or not, and only then send it
    val richArticle = RichArticle()
    richArticle.id = FeedUtil.toURI("maintenance-request", url, Date())
    richArticle.title = "Maintenance required"
    richArticle.contentText = e.message ?: e.toString()
    richArticle.url = "${apiGatewayUrl}/?reason=${e.message}&url=${encode(url)}"
    richArticle.publishedAt = Date()
    return richArticle
  }

  private fun validateVersion(version: String) {
    if (version != propertyService.webToFeedVersion) {
      throw RuntimeException("Invalid webToFeed Version. Got ${version}, expected ${propertyService.webToFeedVersion}")
    }
  }
}
