package org.migor.feedless.pipeline.plugins

import org.apache.commons.lang3.BooleanUtils
import org.migor.feedless.actions.toDto
import org.migor.feedless.data.jpa.models.SourceSubscriptionEntity
import org.migor.feedless.data.jpa.models.WebDocumentEntity
import org.migor.feedless.generated.types.DOMElementByXPath
import org.migor.feedless.generated.types.FeedlessPlugins
import org.migor.feedless.generated.types.PluginExecution
import org.migor.feedless.generated.types.PluginExecutionParamsInput
import org.migor.feedless.generated.types.ScrapeEmit
import org.migor.feedless.generated.types.ScrapePage
import org.migor.feedless.generated.types.ScrapePrerender
import org.migor.feedless.generated.types.ScrapeRequest
import org.migor.feedless.generated.types.ScrapeSelector
import org.migor.feedless.generated.types.ScrapeSelectorExpose
import org.migor.feedless.generated.types.ScrapedElement
import org.migor.feedless.generated.types.ScrapedReadability
import org.migor.feedless.pipeline.FragmentTransformerPlugin
import org.migor.feedless.pipeline.MapEntityPlugin
import org.migor.feedless.service.ScrapeService
import org.migor.feedless.util.HtmlUtil
import org.migor.feedless.web.WebToArticleTransformer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service

@Service
class FulltextPlugin : MapEntityPlugin, FragmentTransformerPlugin {

  private val log = LoggerFactory.getLogger(FulltextPlugin::class.simpleName)

  @Autowired
  lateinit var webToArticleTransformer: WebToArticleTransformer

  @Lazy
  @Autowired
  lateinit var scrapeService: ScrapeService


  override fun id(): String = FeedlessPlugins.org_feedless_fulltext.name
  override fun name(): String = "Fulltext & Readability"

  override fun listed() = true

  override fun mapEntity(
    corrId: String,
    webDocument: WebDocumentEntity,
    subscription: SourceSubscriptionEntity,
    params: PluginExecutionParamsInput
  ) {
    log.info("[$corrId] mapEntity ${webDocument.url}")

    val emit = ScrapeEmit.newBuilder()
      .selectorBased(
        ScrapeSelector.newBuilder()
          .xpath(DOMElementByXPath.newBuilder().value("/").build())
          .expose(
            ScrapeSelectorExpose.newBuilder()
              .build()
          )
          .build()
      )
      .build()
    val request = ScrapeRequest.newBuilder()
      .page(
        ScrapePage.newBuilder()
          .url(webDocument.url)
          .build()
      )
      .emit(listOf(emit))
      .build()

    val source = subscription.sources[0]
    if (BooleanUtils.isTrue(params.org_feedless_fulltext.inheritParams) && source.prerender) {
      request.page.actions = source.actions.map { it.toDto() }
      request.page.prerender = ScrapePrerender.newBuilder()
        .language(source.language)
        .viewport(source.viewport)
        .additionalWaitSec(source.additionalWaitSec ?: 0)
        .waitUntil(source.waitUntil)
        .build()
      request.emit = emptyList()
    }

    val response = scrapeService.scrape(corrId, request)
      .block()!!


    if (!response.failed && response.elements.isNotEmpty()) {
      val element = response.elements.first()!!
      val html = element.selector.html.data
      if (params.org_feedless_fulltext.readability) {
        val readability = webToArticleTransformer.fromHtml(html, webDocument.url)
        webDocument.contentHtml = readability.content
        webDocument.contentText = readability.contentText
        webDocument.contentTitle = readability.title
      } else {
        webDocument.contentHtml = html
        webDocument.contentTitle = HtmlUtil.parseHtml(html, webDocument.url).title()
      }
    }
  }

  override fun transformFragment(
    corrId: String,
    element: ScrapedElement,
    plugin: PluginExecution,
    url: String
  ): ScrapedReadability {
    val markup = element.selector.html.data
    val article = webToArticleTransformer.fromHtml(markup, url)
    return ScrapedReadability.newBuilder()
      .date(article.date)
      .content(article.content)
      .url(article.url)
      .contentText(article.contentText)
      .contentMime(article.contentMime)
      .faviconUrl(article.faviconUrl)
      .imageUrl(article.imageUrl)
      .title(article.title)
      .build()

  }
}
