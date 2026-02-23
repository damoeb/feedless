package org.migor.feedless.pipeline.plugins

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.currentCoroutineContext
import org.apache.commons.lang3.BooleanUtils
import org.apache.commons.lang3.StringUtils
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.SiteNotFoundException
import org.migor.feedless.actions.ExecuteAction
import org.migor.feedless.actions.ExtractBoundingBoxAction
import org.migor.feedless.actions.ExtractXpathAction
import org.migor.feedless.actions.FetchAction
import org.migor.feedless.actions.ScrapeAction
import org.migor.feedless.common.HttpResponse
import org.migor.feedless.document.Document
import org.migor.feedless.generated.types.FeedlessPlugins
import org.migor.feedless.pipeline.FragmentOutput
import org.migor.feedless.pipeline.FragmentTransformerPlugin
import org.migor.feedless.pipeline.MapEntityPlugin
import org.migor.feedless.repository.Repository
import org.migor.feedless.scrape.LogCollector
import org.migor.feedless.scrape.ScrapeService
import org.migor.feedless.scrape.WebToArticleTransformer
import org.migor.feedless.scrape.needsPrerendering
import org.migor.feedless.source.Source
import org.migor.feedless.source.SourceId
import org.migor.feedless.source.SourceRepository
import org.migor.feedless.util.HtmlUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime

data class FulltextPluginParams(
  @SerializedName("readability") val readability: Boolean,
  @SerializedName("summary") val summary: Boolean,
  @SerializedName("inheritParams") val inheritParams: Boolean,
  @SerializedName("onErrorRemove") val onErrorRemove: Boolean? = null,
)

@Service
@Profile("${AppProfiles.scrape} & ${AppLayer.service}")
class FulltextPlugin : MapEntityPlugin<FulltextPluginParams>, FragmentTransformerPlugin {

  private val log = LoggerFactory.getLogger(FulltextPlugin::class.simpleName)

  @Autowired
  private lateinit var webToArticleTransformer: WebToArticleTransformer

  @Autowired
  private lateinit var sourceRepository: SourceRepository

  @Lazy
  @Autowired
  private lateinit var scrapeService: ScrapeService

  override fun id(): String = FeedlessPlugins.org_feedless_fulltext.name
  override fun name(): String = "Fulltext & Readability"
  override fun listed() = true

  override suspend fun mapEntity(
    document: Document,
    repository: Repository,
    params: FulltextPluginParams,
    logCollector: LogCollector
  ): Document {
    logCollector.log("mapEntity ${document.url}")

    return if (StringUtils.isBlank(document.url)) {
      logCollector.log("skipping, url is empty")
      document
    } else {
      val request = Source(
        id = SourceId(),
        title = "Feed from ${document.url}",
        repositoryId = repository.id,
        createdAt = LocalDateTime.now(),
        actions = emptyList(),
      )


      val fetchAction = FetchAction(
        sourceId = SourceId(),
        url = document.url,
      )

      val source = document.source(sourceRepository)
      val prerender = source?.let { source -> needsPrerendering(source, 0) } == true

      val requestWithAction = if (BooleanUtils.isTrue(params.inheritParams) && prerender) {
        logCollector.log("inheritParams from source")
        request.copy(actions = mergeWithSourceActions(fetchAction, source.actions))
      } else {
        request.copy(actions = listOf(fetchAction))
      }

      try {
        val scrapeOutput = scrapeService.scrape(requestWithAction, logCollector)

        if (scrapeOutput.outputs.isNotEmpty()) {
          val lastOutput = scrapeOutput.outputs.last()
          val html = lastOutput.fetch!!.response.responseBody.toString(StandardCharsets.UTF_8)
          if (params.readability || params.summary) {
            logCollector.log("convert to readability/summary")
            val readability = webToArticleTransformer.fromHtml(
              html,
              document.url.replace(Regex("#[^/]+$"), ""),
              params.summary
            )
            log.debug("${document.id} title ${document.title} -> ${readability.title}")

            document.copy(
              html = readability.html,
              text = StringUtils.trimToEmpty(readability.text),
              title = readability.title
            )
          } else {
            document.copy(
              html = html,
              title = HtmlUtil.parseHtml(html, document.url).title()
            )
          }
        }
      } catch (e: Exception) {
        if (e !is SiteNotFoundException) {
//                    document.url = ""
          throw e
        }
      }
      document
    }
  }

  override suspend fun mapEntity(
    document: Document,
    repository: Repository,
    paramsJson: String?,
    logCollector: LogCollector
  ): Document {
    return mapEntity(document, repository, fromJson(paramsJson), logCollector)
  }

  override suspend fun fromJson(jsonParams: String?): FulltextPluginParams {
    return Gson().fromJson(jsonParams, FulltextPluginParams::class.java)
  }

  suspend fun mergeWithSourceActions(
    fetchAction: FetchAction,
    sourceActions: List<ScrapeAction>
  ): List<ScrapeAction> {
    return if (sourceActions.isEmpty()) {
      listOf(fetchAction)
    } else {
      val cleanedActions = sourceActions.sortedBy { it.pos }
        .filter { it !is ExecuteAction && it !is ExtractBoundingBoxAction && it !is ExtractXpathAction }
        .toMutableList()
      // replace fetch
      cleanedActions[cleanedActions.indexOfFirst { it is FetchAction }] = fetchAction
      cleanedActions
    }
  }

  override suspend fun transformFragment(
    action: ExecuteAction,
    data: HttpResponse,
    logger: LogCollector,
  ): FragmentOutput {
    val markup = data.responseBody.toString(StandardCharsets.UTF_8)
    return FragmentOutput(
      fragmentName = "fulltext",
      items = listOf(
        webToArticleTransformer.fromHtml(
          markup,
          data.url,
          fromJson(action.executorParams!!.paramsJsonString).summary
        )
      ),
    )
  }
}

private fun Document.source(sourceRepository: SourceRepository): Source? {
  return sourceId?.let { sourceRepository.findByIdWithActions(it) }
}
