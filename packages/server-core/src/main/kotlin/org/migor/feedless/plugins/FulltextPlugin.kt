package org.migor.feedless.plugins

import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.models.WebDocumentEntity
import org.migor.feedless.generated.types.FeedlessPlugins
import org.migor.feedless.generated.types.ScrapedElement
import org.migor.feedless.generated.types.ScrapedReadability
import org.migor.feedless.generated.types.Transformer
import org.migor.feedless.service.HttpService
import org.migor.feedless.util.HtmlUtil
import org.migor.feedless.util.JsonUtil
import org.migor.feedless.web.WebToArticleTransformer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.util.*

data class FulltextPluginParams(val readability: Boolean)

@Service
@Profile(AppProfiles.database)
class FulltextPlugin: EntityTransformerPlugin, FragmentTransformerPlugin {

  private val log = LoggerFactory.getLogger(FulltextPlugin::class.simpleName)

  @Autowired
  lateinit var webToArticleTransformer: WebToArticleTransformer

  @Autowired
  lateinit var httpService: HttpService

  override fun id(): String = FeedlessPlugins.org_feedless_fulltext.name
  override fun description(): String = ""

  override fun name(): String = "Fulltext & Readability"

  private val defaultConfig = FulltextPluginParams(true)

  override fun transformEntity(corrId: String, webDocument: WebDocumentEntity, paramsRaw: String?) {
    val params = parseParams(paramsRaw)

    val response = httpService.httpGet(corrId, webDocument.url, 200)
    if (response.contentType.startsWith("text/html")) {
      val html = String(response.responseBody)
      if (params.readability) {
        val readability = webToArticleTransformer.fromHtml(html, webDocument.url)
        webDocument.contentRaw = readability.content
        webDocument.contentRawMime = readability.contentMime
        webDocument.contentText = readability.contentText
        webDocument.contentTitle = readability.title
      } else {
        webDocument.contentRawMime = response.contentType
        webDocument.contentRaw = html
        webDocument.contentTitle = HtmlUtil.parseHtml(html, webDocument.url).title()
      }
    }
  }

  override fun transformFragment(
    corrId: String,
    element: ScrapedElement,
    transformer: Transformer,
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

  private fun parseParams(paramsRaw: String?): FulltextPluginParams {
    return paramsRaw?.let {
      JsonUtil.gson.fromJson(it, FulltextPluginParams::class.java)
    } ?: defaultConfig
  }

}
