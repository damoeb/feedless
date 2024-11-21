package org.migor.feedless.pipeline.plugins

import org.apache.commons.lang3.BooleanUtils
import org.apache.commons.lang3.StringUtils
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.SiteNotFoundException
import org.migor.feedless.actions.ExecuteActionEntity
import org.migor.feedless.actions.ExtractBoundingBoxActionEntity
import org.migor.feedless.actions.ExtractXpathActionEntity
import org.migor.feedless.actions.FetchActionEntity
import org.migor.feedless.actions.PluginExecutionJsonEntity
import org.migor.feedless.actions.ScrapeActionEntity
import org.migor.feedless.common.HttpResponse
import org.migor.feedless.document.DocumentEntity
import org.migor.feedless.generated.types.FeedlessPlugins
import org.migor.feedless.pipeline.FragmentOutput
import org.migor.feedless.pipeline.FragmentTransformerPlugin
import org.migor.feedless.pipeline.MapEntityPlugin
import org.migor.feedless.repository.RepositoryEntity
import org.migor.feedless.scrape.LogCollector
import org.migor.feedless.scrape.ScrapeService
import org.migor.feedless.scrape.WebToArticleTransformer
import org.migor.feedless.scrape.needsPrerendering
import org.migor.feedless.source.SourceEntity
import org.migor.feedless.user.corrId
import org.migor.feedless.util.HtmlUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.nio.charset.StandardCharsets
import kotlin.coroutines.coroutineContext

@Service
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.scrape} & ${AppLayer.service}")
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
    document: DocumentEntity,
    repository: RepositoryEntity,
    params: PluginExecutionJsonEntity,
    logCollector: LogCollector
  ): DocumentEntity {
    val corrId = coroutineContext.corrId()
    logCollector.log("[$corrId] mapEntity ${document.url}")

    return if (StringUtils.isBlank(document.url)) {
      logCollector.log("[$corrId] skipping, url is empty")
      document
    } else {
      val request = SourceEntity()
      request.title = "Feed from ${document.url}"
      val fetchAction = FetchActionEntity()
      fetchAction.url = document.url

      val prerender = document.source?.let { source -> needsPrerendering(source, 0) } ?: false
      if (BooleanUtils.isTrue(params.org_feedless_fulltext!!.inheritParams) && prerender) {
        logCollector.log("[$corrId] inheritParams from source")
        request.actions = mergeWithSourceActions(fetchAction, document.source!!.actions).toMutableList()
      } else {
        request.actions = mutableListOf(fetchAction)
      }

      try {
        val scrapeOutput = scrapeService.scrape(request, logCollector)

        if (scrapeOutput.outputs.isNotEmpty()) {
          val lastOutput = scrapeOutput.outputs.last()
          val html = lastOutput.fetch!!.response.responseBody.toString(StandardCharsets.UTF_8)
          if (params.org_feedless_fulltext.readability || params.org_feedless_fulltext.summary) {
            logCollector.log("[$corrId] convert to readability/summary")
            val readability = webToArticleTransformer.fromHtml(
              html,
              document.url.replace(Regex("#[^/]+$"), ""),
              params.org_feedless_fulltext.summary
            )
            document.html = readability.html
            document.text = StringUtils.trimToEmpty(readability.text)
            log.debug("${document.id} title ${document.title} -> ${readability.title}")
            document.title = readability.title
          } else {
            document.html = html
            document.title = HtmlUtil.parseHtml(html, document.url).title()
          }
        }
      } catch (e: Exception) {
        if (e !is SiteNotFoundException) {
          document.url = ""
          throw e
        }
      }
      document
    }
  }

  suspend fun mergeWithSourceActions(
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

  override suspend fun transformFragment(
    action: ExecuteActionEntity,
    data: HttpResponse,
    logger: LogCollector,
  ): FragmentOutput {
    val markup = data.responseBody.toString(StandardCharsets.UTF_8)
    return FragmentOutput(
      fragmentName = "fulltext",
      items = listOf(
        webToArticleTransformer.fromHtml(markup, data.url, action.executorParams!!.org_feedless_fulltext!!.summary)
      ),
    )
  }
}
