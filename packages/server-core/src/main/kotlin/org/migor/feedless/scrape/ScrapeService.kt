package org.migor.feedless.scrape

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.lang3.StringUtils
import org.migor.feedless.AppLayer
import org.migor.feedless.AppMetrics
import org.migor.feedless.AppProfiles
import org.migor.feedless.ResumableHarvestException
import org.migor.feedless.actions.ClickPositionAction
import org.migor.feedless.actions.ClickXpathAction
import org.migor.feedless.actions.DomAction
import org.migor.feedless.actions.DomEventType
import org.migor.feedless.actions.ExecuteAction
import org.migor.feedless.actions.ExtractBoundingBoxAction
import org.migor.feedless.actions.ExtractXpathAction
import org.migor.feedless.actions.FetchAction
import org.migor.feedless.actions.HeaderAction
import org.migor.feedless.actions.ScrapeAction
import org.migor.feedless.agent.AgentService
import org.migor.feedless.common.HttpResponse
import org.migor.feedless.common.HttpService
import org.migor.feedless.data.jpa.source.SourceEntity
import org.migor.feedless.data.jpa.source.actions.ClickPositionActionEntity
import org.migor.feedless.data.jpa.source.actions.ClickXpathActionEntity
import org.migor.feedless.data.jpa.source.actions.DomActionEntity
import org.migor.feedless.data.jpa.source.actions.ExecuteActionEntity
import org.migor.feedless.data.jpa.source.actions.ExtractBoundingBoxActionEntity
import org.migor.feedless.data.jpa.source.actions.ExtractXpathActionEntity
import org.migor.feedless.data.jpa.source.actions.FetchActionEntity
import org.migor.feedless.data.jpa.source.actions.HeaderActionEntity
import org.migor.feedless.data.jpa.source.actions.ScrapeActionEntity
import org.migor.feedless.data.jpa.source.toDomain
import org.migor.feedless.generated.types.FetchActionDebugResponse
import org.migor.feedless.generated.types.HttpFetchResponse
import org.migor.feedless.generated.types.LogStatement
import org.migor.feedless.generated.types.ScrapeExtractFragment
import org.migor.feedless.generated.types.ScrapeExtractFragmentPart
import org.migor.feedless.generated.types.ScrapeExtractResponse
import org.migor.feedless.generated.types.ScrapeOutputResponse
import org.migor.feedless.generated.types.TextData
import org.migor.feedless.generated.types.ViewPort
import org.migor.feedless.pipeline.FilterEntityPlugin
import org.migor.feedless.pipeline.FragmentOutput
import org.migor.feedless.pipeline.FragmentTransformerPlugin
import org.migor.feedless.pipeline.Plugin
import org.migor.feedless.pipeline.PluginService
import org.migor.feedless.source.ExtractEmit
import org.migor.feedless.source.Source
import org.migor.feedless.user.corrId
import org.migor.feedless.util.HtmlUtil
import org.migor.feedless.util.toMillis
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import kotlin.coroutines.coroutineContext

class LogCollector {
    val logs = mutableListOf<LogStatement>()
    fun log(message: String) {
        logs.add(LogStatement(message = message, time = LocalDateTime.now().toMillis()))
//    log?.debug("[$corrId] $message")
    }
}


@Service
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.scrape} & ${AppLayer.service}")
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

    suspend fun scrape(source: Source, logCollector: LogCollector): ScrapeOutput {
        return withContext(Dispatchers.IO) {
            val corrId = kotlin.coroutines.coroutineContext.corrId()
            try {
                val scrapeContext = ScrapeContext(logCollector)

                assert(source.actions.isNotEmpty()) { "no actions present" }

                val fetch = source.findFirstFetchOrNull()!!

                logCollector.log("scrape ${source.id} ${fetch.resolveUrl()}")

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
                                    is FetchAction -> handleFetch(source, index, action, context)
                                    is HeaderAction -> handleHeader(action, context)
                                    is DomAction -> handleDomAction(index, action, context)
                                    is ClickXpathAction -> handleClickXpathAction(action, context)
                                    is ExtractXpathAction -> handleExtract(index, action, context)
                                    is ExecuteAction -> handlePluginExecution(index, action, context)
                                    else -> noopAction(action)
                                }
                            }
                            context
                        }
                    }

                log.debug("[$corrId] scraping done")

                ScrapeOutput(
                    context.outputsAsList(),
                    time = System.nanoTime().minus(startTime).div(1000000).toInt()
                )
            } catch (e: Exception) {
                log.warn("scrape failed " + e.message)
                if (e !is ResumableHarvestException) {
                    log.debug("[$corrId] scrape failed for source ${source.id} ${e.message}")
                }
                throw e
            }
        }
    }

    private fun noopAction(action: ScrapeAction) {
        log.info("noop action $action")
    }

    private fun handleClickXpathAction(action: ClickXpathAction, context: ScrapeContext) {
        context.log("handleClickXpathAction $action")

    }

    private suspend fun handlePluginExecution(
        index: Int,
        action: ExecuteAction,
        context: ScrapeContext
    ) {
        val corrId = coroutineContext.corrId()
        context.log("[$corrId] handlePluginExecution ${action.pluginId}")

    val plugin = pluginService.resolveById<Plugin>(action.pluginId)
        try {
            when (plugin) {
                is FragmentTransformerPlugin -> {
                    val data = context.lastOutput().fetch?.let {
                        context.log("using fetch response")
                        it.response
                    } ?: run {
                        context.log("using last fragment")
                        context.lastOutput().fragment!!.fragments!!.last()
                    }
                        .toHttpResponse(context.firstUrl()!!)
                    context.setOutputAt(
                        index, ScrapeActionOutput(
                            index = index,
                            fragment = plugin.transformFragment(action, data, context.logCollector),
                        )
                    )
                }

                is FilterEntityPlugin<*> -> run {
                    val output = context.lastOutput()

                    if (output.fragment?.items == null) {
                        throw IllegalArgumentException("plugin '${action.pluginId}' expects fragments items")
                    }

                    context.log("""filter params: ${action.executorParams}""")
                    val result = ScrapeActionOutput(
                        index = index,
                        fragment = FragmentOutput(
                            fragmentName = "filter",
                            items = output.fragment.items.filterIndexed { i, item ->
                                plugin.filterEntity(
                                    item,
                                    action.executorParams!!.paramsJsonString,
                                    i,
                                    context.logCollector
                                )
                            }
                        )
                    )
                    context.setOutputAt(index, result)
                }

                else -> throw IllegalArgumentException("plugin '${action.pluginId}' does not exist ($corrId)")
            }
        } catch (e: Exception) {
            context.log(e.stackTraceToString())
            log.warn("[$corrId] handlePluginExecution error ${e.message}")
            throw e
        }
    }

    private suspend fun handleDomAction(index: Int, action: DomAction, context: ScrapeContext) {
        context.log("handleDomAction $action")
        when (action.event) {
            DomEventType.purge -> handlePurgeAction(index, action, context)
            else -> log.warn("cannot handle dom-action ${action.event}")
        }
    }

    private fun handlePurgeAction(index: Int, action: DomAction, context: ScrapeContext) {
        purgeOrExtract(index, action.xpath, true, emptyArray(), context)
    }

    private fun handleExtract(index: Int, action: ExtractXpathAction, context: ScrapeContext) {
        if (action.emit.contains(ExtractEmit.pixel)) {
            this.noopAction(action)
        } else {
            context.log("handleExtract $action")
            purgeOrExtract(index, action.xpath, false, action.emit, context)
        }
    }

    private fun purgeOrExtract(

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
            context.log("purge xpath '${xpath}' -> ${elements.size} elements")
            elements.remove()
            listOf(
                ScrapeExtractFragment(
                    html = TextData(document.html()),
                    uniqueBy = ScrapeExtractFragmentPart.html
                )
            )
        } else {
            context.log("extract xpath '${xpath}' -> ${elements.size} elements")
            elements.mapNotNull {
                if (emit.contains(ExtractEmit.html)) {
                    ScrapeExtractFragment(html = TextData(it.outerHtml()), uniqueBy = ScrapeExtractFragmentPart.html)
                } else {
                    if (emit.contains(ExtractEmit.text)) {
                        ScrapeExtractFragment(text = TextData(it.text()), uniqueBy = ScrapeExtractFragmentPart.text)
                    } else {
                        null
                    }
                }
            }
        }
        val result = ScrapeActionOutput(
            index = index,
            fragment = FragmentOutput(
                fragmentName = "",
                fragments = fragments,
            )
        )

        context.setOutputAt(index, result)
    }


    private suspend fun handleHeader(action: HeaderAction, context: ScrapeContext) {
        context.log("[${coroutineContext.corrId()}] handleHeader $action")
        context.headers[action.name] = action.value
    }

    private suspend fun handleFetch(
        source: Source,
        index: Int,
        action: FetchAction,
        context: ScrapeContext
    ) {
        val corrId = coroutineContext.corrId()
        context.log("[$corrId] handleFetch $action")
        val prerender = needsPrerendering(source, index)
        if (prerender) {
            context.log("[$corrId] send to agent")
            val response = agentService.prerender(source).get()
            response.outputs.map { it.fromDto() }.forEach { scrapeActionOutput ->
//        log.info("[$corrId] outputs @$outputIndex")
                context.setOutputAt(scrapeActionOutput.index, scrapeActionOutput)
            }
            context.logCollector.logs.addAll(response.logs.map {
                LogStatement(
                    time = it.time,
                    message = "[agent] ${it.message}"
                )
            })
            context.log("[$corrId] received -> ${response.outputs.size} outputs")
            log.debug("[$corrId] received -> ${response.outputs.size} outputs")
        } else {
            context.log("[$corrId] render static")
//      val startTime = System.nanoTime()
            val url = action.resolveUrl()
//      httpService.guardedHttpResource(
//        corrId,
//        url,
//        200,
//        listOf("text/", "application/xml", "application/json", "application/rss", "application/atom", "application/rdf")
//      )
            assert(StringUtils.isNotBlank(url))
            val staticResponse = httpService.httpGetCaching(url, 200, context.headers)
            val debug = FetchActionDebugResponse(
                corrId = "",
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

fun needsPrerendering(source: Source, currentActionIndex: Int): Boolean {
    val actions = source.actions.filterIndexed { index, _ -> index >= currentActionIndex }
    val hasClickPosition = actions.filterIsInstance<ClickPositionAction>().isNotEmpty()
    val hasExtractBbox = actions.filterIsInstance<ExtractBoundingBoxAction>().isNotEmpty()
    val hasPixel = actions.filterIsInstance<ExtractXpathAction>().any { it.emit.contains(ExtractEmit.pixel) }
    val hasForcedPrerendering = actions.filterIsInstance<FetchAction>().any { it.forcePrerender }
    return hasClickPosition || hasExtractBbox || hasForcedPrerendering || hasPixel
}


private fun FetchAction.resolveUrl(): String {
    assert(!isVariable) { "variable ref not supported" }
    return url
}

private fun Source.findFirstFetchOrNull(): FetchAction? {
    return actions.filterIsInstance<FetchAction>().firstOrNull()
}
