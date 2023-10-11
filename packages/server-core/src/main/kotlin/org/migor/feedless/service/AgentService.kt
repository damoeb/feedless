package org.migor.feedless.service

import io.micrometer.core.instrument.MeterRegistry
import org.migor.feedless.AppMetrics
import org.migor.feedless.api.auth.TokenProvider
import org.migor.feedless.api.graphql.DtoResolver
import org.migor.feedless.generated.types.Agent
import org.migor.feedless.generated.types.AgentAuthentication
import org.migor.feedless.generated.types.AgentEvent
import org.migor.feedless.generated.types.OsInfo
import org.migor.feedless.generated.types.RegisterAgentInput
import org.migor.feedless.generated.types.ScrapeRequest
import org.migor.feedless.generated.types.ScrapeResponse
import org.migor.feedless.generated.types.ScrapeResponseInput
import org.migor.feedless.harvest.ResumableHarvestException
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink
import reactor.core.publisher.Mono
import java.time.Duration
import java.util.*

@Service
class AgentService : PuppeteerService {
  private val log = LoggerFactory.getLogger(AgentService::class.simpleName)
  private val agentRefs: ArrayList<AgentRef> = ArrayList()
  private val pendingJobs: MutableMap<String, FluxSink<ScrapeResponse>> = mutableMapOf()

  @Autowired
  lateinit var userSecretService: UserSecretService

  @Autowired
  lateinit var tokenProvider: TokenProvider

  @Autowired
  lateinit var propertyService: PropertyService

  @Autowired
  lateinit var meterRegistry: MeterRegistry

  fun registerPrerenderAgent(data: RegisterAgentInput): Publisher<AgentEvent> {
    return Flux.create { emitter ->
      userSecretService.findBySecretKeyValue(data.secretKey.secretKey, data.secretKey.email)
        ?.let {
            securityKey ->
          if (securityKey.validUntil.before(Date())) {
            emitter.error(IllegalAccessException("Key is expired"))
            emitter.complete()
          } else {
            userSecretService.updateLastUsed(securityKey.id, Date())
            val agentRef = AgentRef(securityKey.id, securityKey.ownerId!!, data.version, data.os, Date(), emitter)

            emitter.onDispose {
              removeAgent(agentRef)
            }
            emitter.next(
              AgentEvent.newBuilder()
                .authentication(
                  AgentAuthentication.newBuilder()
                    .token(tokenProvider.createJwtForAgent(securityKey).tokenValue)
                    .build()
                ).build()
            )
            addAgent(agentRef)
          }
        }
        ?: run {
          emitter.error(IllegalAccessException("user/key combination not found or account locked"))
          emitter.complete()
        }
    }
  }

  private fun addAgent(agentRef: AgentRef) {
    agentRefs.add(agentRef)
    log.info("Added Agent (${agentRefs.size}) ${agentRef.toString()}")
    meterRegistry.gauge(AppMetrics.agentCounter, 0)?.inc()
  }

  private fun removeAgent(agentRef: AgentRef) {
    agentRefs.remove(agentRef)
    log.info("Removed Agent (${agentRefs.size})")
    meterRegistry.gauge(AppMetrics.agentCounter, 0)?.dec()
  }

  override fun canPrerender(): Boolean = agentRefs.isNotEmpty()

  override fun prerender(corrId: String, scrapeRequest: ScrapeRequest): Mono<ScrapeResponse> {
    return if (canPrerender()) {
      val agentRef = agentRefs[(Math.random() * agentRefs.size).toInt()]
      prerenderWithAgent(corrId, scrapeRequest, agentRef)
    } else {
      Mono.error(ResumableHarvestException("No agents available", Duration.ofMinutes(10)))
    }
  }

  private fun prerenderWithAgent(
    corrId: String,
    scrapeRequest: ScrapeRequest,
    agentRef: AgentRef
  ): Mono<ScrapeResponse> {
    return Flux.create { emitter ->
      run {
        val harvestJobId = UUID.randomUUID().toString()
        scrapeRequest.id = harvestJobId
        scrapeRequest.corrId = corrId
        agentRef.emitter.next(
          AgentEvent.newBuilder()
            .scrape(scrapeRequest)
            .build()
        )
        log.info("$corrId] trigger agent job $harvestJobId")
        pendingJobs[harvestJobId] = emitter
      }
    }
      .timeout(Duration.ofSeconds(60))
      .next()
  }

  fun handleScrapeResponse(corrId: String, harvestJobId: String, scrapeResponse: ScrapeResponseInput) {
    log.info("[$corrId] handleScrapeResponse $harvestJobId, err=${scrapeResponse.errorMessage}")
    pendingJobs[harvestJobId]?.let {
      if (scrapeResponse.failed) {
        it.error(IllegalArgumentException(scrapeResponse.errorMessage))
      } else {
        it.next(DtoResolver.fromDto(scrapeResponse))
      }
      pendingJobs.remove(harvestJobId)
    } ?: log.error("[$corrId] emitter for job ID not found (${pendingJobs.size} pending jobs)")
  }

  fun findAll(): List<Agent> {
    return agentRefs.map { Agent.newBuilder()
      .secretKeyId(it.secretKeyId.toString())
      .version(it.version)
      .ownerId(it.ownerId.toString())
      .addedAt(it.addedAt.time)
      .osInfo("${it.os.platform}-${it.os.arch}")
      .build()
    }
  }

}

data class AgentRef(
  val secretKeyId: UUID,
  val ownerId: UUID,
  val version: String,
  val os: OsInfo,
  val addedAt: Date,
  val emitter: FluxSink<AgentEvent>
) {
  override fun toString(): String {
    return "AgentRef(secretKeyId=$secretKeyId, ownerId=$ownerId, version='$version', os=$os)"
  }
}
