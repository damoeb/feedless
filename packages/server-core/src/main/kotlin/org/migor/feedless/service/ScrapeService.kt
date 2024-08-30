package org.migor.feedless.service

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import org.migor.feedless.AppMetrics
import org.migor.feedless.AppProfiles
import org.migor.feedless.ResumableHarvestException
import org.migor.feedless.actions.ClickPositionActionEntity
import org.migor.feedless.actions.ClickXpathActionEntity
import org.migor.feedless.actions.DomActionEntity
import org.migor.feedless.actions.DomEventType
import org.migor.feedless.actions.ExecuteActionEntity
import org.migor.feedless.actions.ExtractBoundingBoxActionEntity
import org.migor.feedless.actions.ExtractEmit
import org.migor.feedless.actions.ExtractXpathActionEntity
import org.migor.feedless.actions.FetchActionEntity
import org.migor.feedless.actions.HeaderActionEntity
import org.migor.feedless.actions.ScrapeActionEntity
import org.migor.feedless.agent.AgentService
import org.migor.feedless.common.HttpResponse
import org.migor.feedless.common.HttpService
import org.migor.feedless.generated.types.FetchActionDebugResponse
import org.migor.feedless.generated.types.HttpFetchResponse
import org.migor.feedless.generated.types.LogStatement
import org.migor.feedless.generated.types.ScrapeExtractFragment
import org.migor.feedless.generated.types.ScrapeExtractResponse
import org.migor.feedless.generated.types.ScrapeOutputResponse
import org.migor.feedless.generated.types.TextData
import org.migor.feedless.generated.types.ViewPort
import org.migor.feedless.pipeline.FeedlessPlugin
import org.migor.feedless.pipeline.FilterEntityPlugin
import org.migor.feedless.pipeline.FragmentOutput
import org.migor.feedless.pipeline.FragmentTransformerPlugin
import org.migor.feedless.pipeline.PluginService
import org.migor.feedless.source.SourceEntity
import org.migor.feedless.util.HtmlUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets


@Service
@Profile(AppProfiles.scrape)
class ScrapeService {

  private val log = LoggerFactory.getLogger(ScrapeService::class.simpleName)

  @Autowired
  private lateinit var httpService: HttpService

  @Autowired
  private lateinit var agentService: AgentService

  @Autowired
  private lateinit var pluginService: PluginService

  @Autowired
  private lateinit var meterRegistry: MeterRegistry

  suspend fun scrape(corrId: String, source: SourceEntity): ScrapeOutput {
    return try {
      val scrapeContext = ScrapeContext(log)

      assert(source.actions.isNotEmpty()) { "no actions present" }

      val fetch = source.findFirstFetchOrNull()!!

      log.debug("[$corrId] scrape ${source.id} ${fetch.resolveUrl()}")

      meterRegistry.counter(
        AppMetrics.scrape, listOf(
          Tag.of("type", "scrape"),
          Tag.of("prerender", needsPrerendering(source, 0).toString()),
        )
      ).increment()

      val startTime = System.nanoTime()

      val context = source.actions
        .sortedBy { it.pos }
        .foldIndexed(scrapeContext) { index, context, action ->
          run {
            if (!context.hasOutputAt(index)) {
              when (action) {
                is FetchActionEntity -> handleFetch(corrId, source, index, action, context)
                is HeaderActionEntity -> handleHeader(corrId, action, context)
                is DomActionEntity -> handleDomAction(corrId, index, action, context)
                is ClickXpathActionEntity -> handleClickXpathAction(corrId, action, context)
                is ExtractXpathActionEntity -> handleExtract(corrId, index, action, context)
                is ExecuteActionEntity -> handlePluginExecution(corrId, index, action, context)
                else -> noopAction(corrId, action)
              }
            }
            context
          }
        }

      log.debug("[$corrId] scraping done")

      ScrapeOutput(
        context.outputsAsList(),
        logs = context.logs,
        time = System.nanoTime().minus(startTime).div(1000000).toInt()
      )
    } catch (e: Exception) {
      if (e !is ResumableHarvestException) {
        log.warn("[$corrId] scrape failed for source ${source.id} ${e.message}")
      }
      throw e
    }
  }

  private fun noopAction(corrId: String, action: ScrapeActionEntity) {
    log.info("[$corrId] noop action $action")
  }

  private fun handleClickXpathAction(corrId: String, action: ClickXpathActionEntity, context: ScrapeContext) {
    context.info("[$corrId] handleClickXpathAction $action")

  }

  private fun handlePluginExecution(corrId: String, index: Int, action: ExecuteActionEntity, context: ScrapeContext) {
    context.info("[$corrId] handlePluginExecution ${action.pluginId}")

    val plugin = pluginService.resolveById<FeedlessPlugin>(action.pluginId)
    try {
      when (plugin) {
        is FragmentTransformerPlugin -> {
          val data = context.lastOutput().fetch?.let {
            context.info("using fetch response")
            it.response } ?: run {
            context.info("using last fragment")
              context.lastOutput().fragment!!.fragments!!.last()
            }
            .toHttpResponse(context.firstUrl()!!)
          context.setOutputAt(index, ScrapeActionOutput(
              index = index,
              fragment = plugin.transformFragment(corrId, action, data) { context.info(it) },
            )
          )
        }
        is FilterEntityPlugin -> run {
          val output = context.lastOutput()

          if (output.fragment?.items == null) {
            throw IllegalArgumentException("plugin '${action.pluginId}' expects fragments items ($corrId)")
          }
          val result = ScrapeActionOutput(index = index,
            fragment = FragmentOutput(
              fragmentName = "filter",
              items = output.fragment.items.filterIndexed { i, item -> plugin.filterEntity(corrId, item, action.executorParams!!, i) }
            )
          )
          context.setOutputAt(index, result)
        }
        else -> throw IllegalArgumentException("plugin '${action.pluginId}' does not exist ($corrId)")
      }
    } catch (e: Exception) {
      context.info(e.stackTraceToString())
      log.warn("[$corrId] handlePluginExecution error ${e.message}")
      throw e
    }
  }

  private fun handleDomAction(corrId: String, index: Int, action: DomActionEntity, context: ScrapeContext) {
    context.info("[$corrId] handleDomAction $action")
    when (action.event) {
      DomEventType.purge -> handlePurgeAction(corrId, index, action, context)
      else -> log.warn("[$corrId] cannot handle dom-action ${action.event}")
    }
  }

  private fun handlePurgeAction(corrId: String, index: Int, action: DomActionEntity, context: ScrapeContext) {
    purgeOrExtract(corrId, index, action.xpath, true, emptyArray(), context)
  }

  private fun handleExtract(corrId: String, index: Int, action: ExtractXpathActionEntity, context: ScrapeContext) {
    if (action.emit.contains(ExtractEmit.pixel)) {
      this.noopAction(corrId, action)
    } else {
      context.info("[$corrId] handleExtract $action")
      purgeOrExtract(corrId, index, action.xpath, false, action.emit, context)
    }
  }

  private fun purgeOrExtract(
    corrId: String,
    index: Int,
    xpath: String,
    purge: Boolean,
    emit: Array<ExtractEmit>,
    context: ScrapeContext
  ) {
    val lastAction = context.lastOutput()
    val response = lastAction.fetch!!.response
    val document = HtmlUtil.parseHtml(response.responseBody.toString(StandardCharsets.UTF_8), response.url)
    HtmlUtil.withAbsoluteUrls(document)
    val elements = document.selectXpath(xpath)
    val fragments = if (purge) {
      context.info("[$corrId] purge xpath '${xpath}' -> ${elements.size} elements")
      elements.remove()
      listOf(
        ScrapeExtractFragment(
          html = TextData(document.html())
        )
      )
    } else {
      context.info("[$corrId] extract xpath '${xpath}' -> ${elements.size} elements")
      elements.map {
        ScrapeExtractFragment(
          html = if (emit.contains(ExtractEmit.html)) {TextData(it.outerHtml())} else {null},
          text = if (emit.contains(ExtractEmit.text)) {TextData(it.text())} else {null},
        )
      }
    }
    val result = ScrapeActionOutput(index = index,
      fragment = FragmentOutput(
        fragmentName = "",
        fragments = fragments,
      )
    )

    context.setOutputAt(index, result)
  }



  private fun handleHeader(corrId: String, action: HeaderActionEntity, context: ScrapeContext) {
    context.info("[$corrId] handleHeader $action")
    context.headers[action.name] = action.value
  }

  private suspend fun handleFetch(
    corrId: String,
    source: SourceEntity,
    index: Int,
    action: FetchActionEntity,
    context: ScrapeContext
  ) {
    context.info("[$corrId] handleFetch $action")
    val prerender = needsPrerendering(source, index)
    if (prerender) {
      context.info("[$corrId] send to agent")
      val response = agentService.prerender(corrId, source).get()
      response.outputs.map { it.fromDto() }.forEach { scrapeActionOutput ->
//        log.info("[$corrId] outputs @$outputIndex")
        context.setOutputAt(scrapeActionOutput.index, scrapeActionOutput)
      }
      context.logs.addAll(response.logs.map { LogStatement(time = it.time, message = "[agent] ${it.message}") })
      context.info("[$corrId] received -> ${response.outputs.size} outputs")
    } else {
      context.info("[$corrId] render static")
//      val startTime = System.nanoTime()
      val url = action.resolveUrl()
//      httpService.guardedHttpResource(
//        corrId,
//        url,
//        200,
//        listOf("text/", "application/xml", "application/json", "application/rss", "application/atom", "application/rdf")
//      )

      val staticResponse = httpService.httpGetCaching(corrId, url, 200, context.headers)
      val debug = FetchActionDebugResponse(
        corrId = corrId,
        statusCode = staticResponse.statusCode,
        contentType = staticResponse.contentType,
        prerendered = false,
        console = emptyList(),
        network = emptyList(),
        url = url,
        viewport = ViewPort(width = -1, height = -1, isMobile = false, isLandscape = false),
//        html = staticResponse.responseBody.toString(StandardCharsets.UTF_8)
      )
      val fetchOutput = HttpFetchOutput(staticResponse, debug = debug)
      context.setOutputAt(index, ScrapeActionOutput(index = index, fetch = fetchOutput))
    }
  }
}

private fun ScrapeExtractFragment.toHttpResponse(url: String): HttpResponse {
  return HttpResponse(
    contentType = "text/html",
    url = url,
    statusCode = 200,
    responseBody = html!!.data.toByteArray()
  )
}

private fun ScrapeOutputResponse.fromDto(): ScrapeActionOutput {
  return ScrapeActionOutput(
    index = index,
//    execute = response.execute,
    fetch = response.fetch?.fromDto(),
    fragment = response.extract?.fromDto()
//    extract = response.extract
  )
}

private fun ScrapeExtractResponse.fromDto(): FragmentOutput {
  return FragmentOutput(
    fragmentName = fragmentName,
    fragments = fragments
  )
}

private fun HttpFetchResponse.fromDto(): HttpFetchOutput {
  return HttpFetchOutput(
    response = HttpResponse(
      contentType = debug.contentType ?: "",
      url = debug.url,
      statusCode = debug.statusCode ?: 0,
      responseBody = data.toByteArray(StandardCharsets.UTF_8)
    ),
    debug = debug
  )
}

fun needsPrerendering(source: SourceEntity, currentActionIndex: Int): Boolean {
  val actions = source.actions.filterIndexed { index, _ -> index >= currentActionIndex }
  val hasClickPosition = actions.filterIsInstance<ClickPositionActionEntity>().isNotEmpty()
  val hasExtractBbox = actions.filterIsInstance<ExtractBoundingBoxActionEntity>().isNotEmpty()
  val hasPixel = actions.filterIsInstance<ExtractXpathActionEntity>().any { it.emit.contains(ExtractEmit.pixel)}
  val hasForcedPrerendering = actions.filterIsInstance<FetchActionEntity>().any { it.forcePrerender }
  return hasClickPosition || hasExtractBbox || hasForcedPrerendering || hasPixel
}


private fun FetchActionEntity.resolveUrl(): String {
  assert(!isVariable) { "variable ref not supported" }
  return url
}

private fun SourceEntity.findFirstFetchOrNull(): FetchActionEntity? {
  return actions.filterIsInstance<FetchActionEntity>().firstOrNull()
}
