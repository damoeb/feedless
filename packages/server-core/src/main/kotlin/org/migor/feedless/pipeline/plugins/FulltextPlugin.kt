package org.migor.feedless.pipeline.plugins

import org.apache.commons.lang3.BooleanUtils
import org.migor.feedless.AppProfiles
import org.migor.feedless.actions.ExecuteActionEntity
import org.migor.feedless.actions.FetchActionEntity
import org.migor.feedless.common.HttpResponse
import org.migor.feedless.data.jpa.models.SourceEntity
import org.migor.feedless.document.DocumentEntity
import org.migor.feedless.generated.types.FeedlessPlugins
import org.migor.feedless.generated.types.PluginExecutionData
import org.migor.feedless.generated.types.PluginExecutionParamsInput
import org.migor.feedless.generated.types.ScrapedReadability
import org.migor.feedless.pipeline.FragmentTransformerPlugin
import org.migor.feedless.pipeline.MapEntityPlugin
import org.migor.feedless.repository.RepositoryEntity
import org.migor.feedless.service.ScrapeService
import org.migor.feedless.service.needsPrerendering
import org.migor.feedless.util.HtmlUtil
import org.migor.feedless.web.WebToArticleTransformer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets

@Service
@Profile(AppProfiles.scrape)
class FulltextPlugin : MapEntityPlugin, FragmentTransformerPlugin {

  private val log = LoggerFactory.getLogger(FulltextPlugin::class.simpleName)

  @Autowired
  private lateinit var webToArticleTransformer: WebToArticleTransformer

  @Lazy
  @Autowired
  private lateinit var scrapeService: ScrapeService


  override fun id(): String = FeedlessPlugins.org_feedless_fulltext.name
  override fun name(): String = "Fulltext & Readability"

  override fun listed() = true

  override fun mapEntity(
    corrId: String,
    document: DocumentEntity,
    repository: RepositoryEntity,
    params: PluginExecutionParamsInput
  ): DocumentEntity {
    log.debug("[$corrId] mapEntity ${document.url}")

    val source = repository.sources[0]

    val request = SourceEntity()
    request.title = "Feed from ${document.url}"
    val action = FetchActionEntity()
    action.url = document.url
    request.actions = mutableListOf(action)
    val prerender = needsPrerendering(source, 0)
    if (BooleanUtils.isTrue(params.org_feedless_fulltext!!.inheritParams) && prerender) {
      log.debug("[$corrId] with inheritParams")
      request.actions.addAll(source.actions.subList(1, source.actions.size))
    }

    val response = scrapeService.scrape(corrId, request)
      .block()!!

    if (response.outputs.isNotEmpty()) {
      val lastOutput = response.outputs.last()
      val html = lastOutput.fetch!!.response.responseBody.toString(StandardCharsets.UTF_8)
      if (params.org_feedless_fulltext.readability) {
        val readability = webToArticleTransformer.fromHtml(html, document.url)
        document.contentHtml = readability.content
        document.contentText = readability.contentText!!
        document.contentTitle = readability.title
      } else {
        document.contentHtml = html
        document.contentTitle = HtmlUtil.parseHtml(html, document.url).title()
      }
    }
    return document
  }

  override fun transformFragment(
    corrId: String,
    action: ExecuteActionEntity,
    data: HttpResponse,
  ): PluginExecutionData {
    val markup = data.responseBody.toString(StandardCharsets.UTF_8)
    val article = webToArticleTransformer.fromHtml(markup, data.url)
    return PluginExecutionData(
      org_feedless_fulltext = ScrapedReadability(
        date = article.date,
        content = article.content,
        url = article.url,
        contentText = article.contentText,
        contentMime = article.contentMime,
        faviconUrl = article.faviconUrl,
        imageUrl = article.imageUrl,
        title = article.title,
      )
    )
  }
}
