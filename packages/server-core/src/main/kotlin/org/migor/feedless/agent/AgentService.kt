package org.migor.feedless.agent

import io.micrometer.core.instrument.MeterRegistry
import org.migor.feedless.AppMetrics
import org.migor.feedless.ResumableHarvestException
import org.migor.feedless.api.fromDto
import org.migor.feedless.common.PropertyService
import org.migor.feedless.generated.types.AgentAuthentication
import org.migor.feedless.generated.types.AgentEvent
import org.migor.feedless.generated.types.OsInfo
import org.migor.feedless.generated.types.RegisterAgentInput
import org.migor.feedless.generated.types.ScrapeRequest
import org.migor.feedless.generated.types.ScrapeResponse
import org.migor.feedless.generated.types.ScrapeResponseInput
import org.migor.feedless.secrets.UserSecretService
import org.migor.feedless.session.TokenProvider
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
class AgentService {
  private val log = LoggerFactory.getLogger(AgentService::class.simpleName)
  private val agentRefs: ArrayList<AgentRef> = ArrayList()
  private val pendingJobs: MutableMap<String, FluxSink<ScrapeResponse>> = mutableMapOf()

  @Autowired
  lateinit var userSecretService: UserSecretService

  @Autowired
  lateinit var tokenProvider: TokenProvider

  @Autowired
  lateinit var agentDAO: AgentDAO

  @Autowired
  lateinit var propertyService: PropertyService

  @Autowired
  lateinit var meterRegistry: MeterRegistry

  fun registerPrerenderAgent(corrId: String, data: RegisterAgentInput): Publisher<AgentEvent> {
    return Flux.create { emitter ->
      userSecretService.findBySecretKeyValue(data.secretKey.secretKey, data.secretKey.email)
        ?.let { securityKey ->
          if (securityKey.validUntil.before(Date())) {
            emitter.error(IllegalAccessException("Key is expired"))
            emitter.complete()
          } else {
            userSecretService.updateLastUsed(securityKey.id, Date())
            val agentRef =
              AgentRef(securityKey.id, securityKey.ownerId, data.version, data.connectionId, data.os, Date(), emitter)

            emitter.onDispose {
              removeAgent(corrId, agentRef)
            }
            emitter.next(
              AgentEvent.newBuilder()
                .authentication(
                  AgentAuthentication.newBuilder()
                    .token(tokenProvider.createJwtForAgent(securityKey).tokenValue)
                    .build()
                ).build()
            )
            addAgent(corrId, agentRef)
          }
        }
        ?: run {
          emitter.error(IllegalAccessException("user/key combination not found or account locked"))
          emitter.complete()
        }
    }
  }

  private fun addAgent(corrId: String, agentRef: AgentRef) {
    agentRefs.add(agentRef)

    log.info("[$corrId] Added Agent $agentRef")

    val agent = AgentEntity()
    agent.secretKeyId = agentRef.secretKeyId
    agent.version = agentRef.version
    agent.connectionId = agentRef.connectionId
    agent.ownerId = agentRef.ownerId
    agent.openInstance = true
    agentDAO.save(agent)

    meterRegistry.gauge(AppMetrics.agentCounter, 0)?.inc()
  }

  private fun removeAgent(corrId: String, agentRef: AgentRef) {
    agentRefs.remove(agentRef)
    log.info("[$corrId] Removed Agent")
    agentDAO.deleteByConnectionIdAndSecretKeyId(agentRef.connectionId, agentRef.secretKeyId)
    meterRegistry.gauge(AppMetrics.agentCounter, 0)?.dec()
  }

  fun hasAgents(): Boolean = agentRefs.isNotEmpty()

  fun prerender(corrId: String, scrapeRequest: ScrapeRequest): Mono<ScrapeResponse> {
    return if (hasAgents()) {
      val agentRef = agentRefs[(Math.random() * agentRefs.size).toInt()]
      prerenderWithAgent(corrId, scrapeRequest, agentRef)
    } else {
      throw ResumableHarvestException(corrId, "No agents available", Duration.ofMinutes(10))
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

        try {
          agentRef.emitter.next(
            AgentEvent.newBuilder()
              .scrape(scrapeRequest)
              .build()
          )
          log.info("$corrId] submitted agent job $harvestJobId")
          pendingJobs[harvestJobId] = emitter
        } catch (e: Exception) {
          log.error("$corrId] ${e.message}")
          emitter.error(e)
        }
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
        it.next(scrapeResponse.fromDto())
      }
      pendingJobs.remove(harvestJobId)
    } ?: log.error("[$corrId] emitter for job ID not found (${pendingJobs.size} pending jobs)")
  }

  fun findAll(userId: UUID): List<AgentEntity> {
    return agentDAO.findAllByOwnerIdOrOpenInstanceIsTrue(userId)
  }
}

data class AgentRef(
  val secretKeyId: UUID,
  val ownerId: UUID,
  val version: String,
  val connectionId: String,
  val os: OsInfo,
  val addedAt: Date,
  val emitter: FluxSink<AgentEvent>
) {
  override fun toString(): String {
    return "AgentRef(connectionId=$connectionId, secretKeyId=$secretKeyId, ownerId=$ownerId, version='$version', os=$os)"
  }
}
