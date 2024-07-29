package org.migor.feedless.service

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import org.migor.feedless.AppMetrics
import org.migor.feedless.AppProfiles
import org.migor.feedless.BadRequestException
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
import org.migor.feedless.data.jpa.models.SourceEntity
import org.migor.feedless.generated.types.FetchActionDebugResponse
import org.migor.feedless.generated.types.HttpFetchResponse
import org.migor.feedless.generated.types.PluginExecutionResponse
import org.migor.feedless.generated.types.ScrapeActionResponse
import org.migor.feedless.generated.types.ScrapeOutputResponse
import org.migor.feedless.generated.types.ViewPort
import org.migor.feedless.pipeline.PluginService
import org.migor.feedless.util.HtmlUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import us.codecraft.xsoup.Xsoup
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

  fun scrape(corrId: String, source: SourceEntity): Mono<ScrapeOutput> {
    assert(source.actions.isNotEmpty()) { "no actions present" }
    val prerender = needsPrerendering(source, 0)

    val fetch = source.findFirstFetchOrNull()!!

    log.info("[$corrId] scrape ${fetch.resolveUrl()}")

    meterRegistry.counter(
      AppMetrics.scrape, listOf(
        Tag.of("type", "scrape"),
        Tag.of("prerender", prerender.toString()),
      )
    ).increment()

    val startTime = System.nanoTime()

    val context = source.actions
      .sortedBy { it.pos }
      .foldIndexed(ScrapeContext()) { index, context, action ->
        run {
          when (action) {
            is FetchActionEntity -> handleFetch(corrId, source, index, action, context)
            is HeaderActionEntity -> handleHeader(corrId, action, context)
            is DomActionEntity -> handleDomAction(corrId, index, action, context)
            is ClickXpathActionEntity -> handleClickXpathAction(corrId, action, context)
            is ExtractXpathActionEntity -> handleExtract(corrId, action, context)
            is ExecuteActionEntity -> handlePluginExecution(corrId, index, action, context)
            else -> noopAction(corrId, action)
          }
          context
        }
      }

    log.info("[$corrId] scraping finished")

    return Mono.just(
      ScrapeOutput(
        context.outputs.values.toList(),
        logs = context.logs,
        time = System.nanoTime().minus(startTime).div(1000000).toInt()
      )
    )
  }

  private fun noopAction(corrId: String, action: ScrapeActionEntity) {
    log.info("[$corrId] noop action $action")
  }

  private fun handleClickXpathAction(corrId: String, action: ClickXpathActionEntity, context: ScrapeContext) {
    log.info("[$corrId] handleClickXpathAction $action")

  }

  private fun handlePluginExecution(corrId: String, index: Int, action: ExecuteActionEntity, context: ScrapeContext) {
    log.info("[$corrId] handlePluginExecution ${action.pluginId}")

    val firstFitchAction = context.outputs.values.find { it.fetch != null }!!
    val plugin = pluginService.resolveFragmentTransformerById(action.pluginId)
    try {
    val data = plugin
      ?.transformFragment(corrId, action, firstFitchAction.fetch!!.response)
      ?: throw BadRequestException("plugin '${action.pluginId}' does not exist ($corrId)")

      val output = PluginExecutionResponse(pluginId = plugin.id(), data = data)
      val result = ScrapeActionOutput(index= index, execute = output)

      context.outputs[index] = result
    } catch (e: Exception) {
      context.logs.add(e.stackTraceToString())
      log.warn("[$corrId] handlePluginExecution error", e)
    }
  }

  private fun handleDomAction(corrId: String, index: Int, action: DomActionEntity, context: ScrapeContext) {
    log.info("[$corrId] handleDomAction $action")
    when (action.event) {
      DomEventType.purge -> handlePurgeAction(corrId, index, action, context)
      else -> log.warn("[$corrId] cannot handle dom-action ${action.event}")
    }
  }

  private fun handlePurgeAction(corrId: String, index: Int, action: DomActionEntity, context: ScrapeContext) {
//    val lastAction = context.outputs.last()
//    val document = HtmlUtil.parseHtml(lastAction.fetch!!.response.responseBody.toString(StandardCharsets.UTF_8), "")
//    val elements = Xsoup.compile(action.xpath).evaluate(document).elements
    log.info("[$corrId] purge element ${action.xpath}")
//    elements.remove()
    TODO()
  }

  private fun handleHeader(corrId: String, action: HeaderActionEntity, context: ScrapeContext) {
    log.info("[$corrId] handleHeader $action")
    context.headers[action.name] = action.value
  }

  private fun handleExtract(corrId: String, action: ExtractXpathActionEntity, context: ScrapeContext) {
    if (action.emit.contains(ExtractEmit.pixel)) {
      this.noopAction(corrId, action)
    } else {
      log.info("[$corrId] handleExtract $action")
      TODO("Not yet implemented")
    }
  }

  private fun handleFetch(
    corrId: String,
    source: SourceEntity,
    index: Int,
    action: FetchActionEntity,
    context: ScrapeContext
  ) {
    log.info("[$corrId] handleFetch $action")
    val prerender = needsPrerendering(source, index)
    if (prerender) {
      log.info("[$corrId] prerender")
      val response = agentService.prerender(corrId, source).block()!!
      log.info("[$corrId] -> ${response.outputs.size} outputs")
      response.outputs.map { it.fromDto() }.forEach() { scrapeActionOutput ->
//        log.info("[$corrId] outputs @$outputIndex")
        context.outputs[scrapeActionOutput.index] = scrapeActionOutput
      }
      context.logs.addAll(response.logs)
    } else {
      log.info("[$corrId] static")
      val startTime = System.nanoTime()
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
      context.outputs[index] = ScrapeActionOutput(index = index, fetch = fetchOutput)
    }
  }
}

private fun ScrapeOutputResponse.fromDto(): ScrapeActionOutput {
  return ScrapeActionOutput(
    index = index,
    execute = response.execute,
    fetch = response.fetch?.fromDto(),
    extract = response.extract
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
