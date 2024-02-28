package org.migor.feedless.plugins

import org.migor.feedless.data.jpa.models.SourceSubscriptionEntity
import org.migor.feedless.data.jpa.models.WebDocumentEntity
import org.migor.feedless.generated.types.FeedlessPlugins
import org.migor.feedless.generated.types.PluginExecution
import org.migor.feedless.generated.types.PluginExecutionParamsInput
import org.migor.feedless.generated.types.ScrapedElement
import org.migor.feedless.generated.types.ScrapedReadability
import org.migor.feedless.service.HttpService
import org.migor.feedless.util.HtmlUtil
import org.migor.feedless.web.WebToArticleTransformer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class FulltextPlugin : MapEntityPlugin, FragmentTransformerPlugin {

  private val log = LoggerFactory.getLogger(FulltextPlugin::class.simpleName)

  @Autowired
  lateinit var webToArticleTransformer: WebToArticleTransformer

  @Autowired
  lateinit var httpService: HttpService
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
    val response = httpService.httpGetCaching(corrId, webDocument.url, 200)
    if (response.contentType.startsWith("text/html")) {
      val html = String(response.responseBody)
      if (params.fulltext.readability) {
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
