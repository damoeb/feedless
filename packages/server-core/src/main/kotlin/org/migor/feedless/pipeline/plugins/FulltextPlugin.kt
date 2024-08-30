package org.migor.feedless.pipeline.plugins

import org.apache.commons.lang3.BooleanUtils
import org.migor.feedless.AppProfiles
import org.migor.feedless.actions.ExecuteActionEntity
import org.migor.feedless.actions.ExtractBoundingBoxActionEntity
import org.migor.feedless.actions.ExtractXpathActionEntity
import org.migor.feedless.actions.FetchActionEntity
import org.migor.feedless.actions.ScrapeActionEntity
import org.migor.feedless.common.HttpResponse
import org.migor.feedless.document.DocumentEntity
import org.migor.feedless.generated.types.FeedlessPlugins
import org.migor.feedless.generated.types.PluginExecutionParamsInput
import org.migor.feedless.pipeline.FragmentOutput
import org.migor.feedless.pipeline.FragmentTransformerPlugin
import org.migor.feedless.pipeline.MapEntityPlugin
import org.migor.feedless.repository.RepositoryEntity
import org.migor.feedless.service.ScrapeService
import org.migor.feedless.service.needsPrerendering
import org.migor.feedless.source.SourceEntity
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

  override suspend fun mapEntity(
    corrId: String,
    document: DocumentEntity,
    repository: RepositoryEntity,
    params: PluginExecutionParamsInput
  ): DocumentEntity {
    log.debug("[$corrId] mapEntity ${document.url}")

    val request = SourceEntity()
    request.title = "Feed from ${document.url}"
    val fetchAction = FetchActionEntity()
    fetchAction.url = document.url
    val prerender = document.source?.let { source -> needsPrerendering(source, 0) } ?: false
    if (BooleanUtils.isTrue(params.org_feedless_fulltext!!.inheritParams) && prerender) {
      log.debug("[$corrId] with inheritParams")
      request.actions = mergeWithSourceActions(fetchAction, document.source!!.actions).toMutableList()
    } else {
      request.actions = mutableListOf(fetchAction)
    }

    val scrapeOutput = scrapeService.scrape(corrId, request)

    if (scrapeOutput.outputs.isNotEmpty()) {
      val lastOutput = scrapeOutput.outputs.last()
      val html = lastOutput.fetch!!.response.responseBody.toString(StandardCharsets.UTF_8)
      if (params.org_feedless_fulltext.readability) {
        val readability = webToArticleTransformer.fromHtml(html, document.url.replace(Regex("#[^/]+$"), ""))
        document.contentHtml = readability.contentHtml
        document.contentText = readability.contentText!!
        document.contentTitle = readability.title
      } else {
        document.contentHtml = html
        document.contentTitle = HtmlUtil.parseHtml(html, document.url).title()
      }
    }
    return document
  }

  fun mergeWithSourceActions(
    fetchAction: FetchActionEntity,
    sourceActions: List<ScrapeActionEntity>
  ): List<ScrapeActionEntity> {
    return if (sourceActions.isEmpty()) {
      listOf(fetchAction)
    } else {
      val cleanedActions = sourceActions.sortedBy { it.pos }
        .filter { it !is ExecuteActionEntity && it !is ExtractBoundingBoxActionEntity && it !is ExtractXpathActionEntity }
        .toMutableList()
      // replace fetch
      cleanedActions[cleanedActions.indexOfFirst { it is FetchActionEntity }] = fetchAction
      cleanedActions
    }
  }

  override fun transformFragment(
      corrId: String,
      action: ExecuteActionEntity,
      data: HttpResponse,
      logger: (String) -> Unit,
  ): FragmentOutput {
    val markup = data.responseBody.toString(StandardCharsets.UTF_8)
    return FragmentOutput(
      fragmentName = "",
      items = listOf(
        webToArticleTransformer.fromHtml(markup, data.url)      ),
    )
  }
}
