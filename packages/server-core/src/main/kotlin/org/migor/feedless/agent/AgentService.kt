package org.migor.feedless.agent

import io.micrometer.core.instrument.MeterRegistry
import org.apache.commons.lang3.StringUtils
import org.migor.feedless.AppMetrics
import org.migor.feedless.AppProfiles
import org.migor.feedless.ResumableHarvestException
import org.migor.feedless.api.fromDto
import org.migor.feedless.generated.types.AgentAuthentication
import org.migor.feedless.generated.types.AgentEvent
import org.migor.feedless.generated.types.OsInfo
import org.migor.feedless.generated.types.RegisterAgentInput
import org.migor.feedless.generated.types.ScrapeResponse
import org.migor.feedless.generated.types.ScrapeResponseInput
import org.migor.feedless.secrets.UserSecretService
import org.migor.feedless.session.TokenProvider
import org.migor.feedless.source.SourceEntity
import org.migor.feedless.source.toDto
import org.migor.feedless.util.JsonUtil
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink
import reactor.core.publisher.Mono
import java.io.Serializable
import java.time.Duration
import java.util.*

class AgentResponse(private val scrapeResponse: String) : Serializable {

  fun get(): ScrapeResponse {
    return JsonUtil.gson.fromJson(scrapeResponse, ScrapeResponse::class.java)
  }
}

@Service
@Profile(AppProfiles.agent)
class AgentService {
  private val log = LoggerFactory.getLogger(AgentService::class.simpleName)
  private val agentRefs: ArrayList<AgentRef> = ArrayList()
  private val pendingJobs: MutableMap<String, FluxSink<AgentResponse>> = mutableMapOf()

  @Autowired
  private lateinit var userSecretService: UserSecretService

  @Autowired
  private lateinit var tokenProvider: TokenProvider

  @Autowired
  private lateinit var agentDAO: AgentDAO

  @Autowired
  private lateinit var meterRegistry: MeterRegistry

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
              AgentRef(
                securityKey.id,
                securityKey.ownerId,
                data.name,
                data.version,
                data.connectionId,
                data.os,
                Date(),
                emitter
              )

            emitter.onDispose {
              removeAgent(corrId, agentRef)
            }
            emitter.next(
              AgentEvent(
                corrId = corrId,
                callbackId = "none",
                authentication = AgentAuthentication(
                  token = tokenProvider.createJwtForAgent(securityKey).tokenValue
                )
              )
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

    agentDAO.deleteByConnectionIdAndSecretKeyId(agentRef.connectionId, agentRef.secretKeyId)

    val agent = AgentEntity()
    agent.secretKeyId = agentRef.secretKeyId
    agent.name = agentRef.name
    agent.version = agentRef.version
    agent.lastSyncedAt = Date()
    agent.connectionId = agentRef.connectionId
    agent.ownerId = agentRef.ownerId
    agent.openInstance = true
    agentDAO.save(agent)

    meterRegistry.gauge(AppMetrics.agentCounter, 0)?.inc()
  }

  private fun removeAgent(corrId: String, agentRef: AgentRef) {
    agentRefs.remove(agentRef)
    log.info("[$corrId] Removing Agent by connectionId=${agentRef.connectionId} and secretKeyId=${agentRef.secretKeyId}")
    agentDAO.deleteByConnectionIdAndSecretKeyId(agentRef.connectionId, agentRef.secretKeyId)
    meterRegistry.gauge(AppMetrics.agentCounter, 0)?.dec()
  }

  fun hasAgents(): Boolean = agentRefs.isNotEmpty()

//  @Cacheable(value = [CacheNames.AGENT_RESPONSE], keyGenerator = "agentResponseCacheKeyGenerator")
  suspend fun prerender(corrId: String, source: SourceEntity): AgentResponse {
    return if (hasAgents()) {
      val agentRef = agentRefs[(Math.random() * agentRefs.size).toInt()]
      prerenderWithAgent(corrId, source, agentRef).block()!!
    } else {
      log.warn("[$corrId] no agents present")
      throw ResumableHarvestException(corrId, "No agents available", Duration.ofMinutes(10))
    }
  }

  private fun prerenderWithAgent(
      corrId: String,
      source: SourceEntity,
      agentRef: AgentRef
  ): Mono<AgentResponse> {
    log.debug("[$corrId] preparing")
    return Flux.create { emitter ->
      try {
        val harvestJobId = UUID.randomUUID().toString()
        agentRef.emitter.next(
          AgentEvent(
            callbackId = harvestJobId,
            corrId = corrId,
            scrape = source.toDto(corrId)
          )
        )
        log.info("[$corrId] submitted agent job $harvestJobId")
        pendingJobs[harvestJobId] = emitter
      } catch (e: Exception) {
        log.error("$corrId] prerenderWithAgent failed: ${e.message}", e)
        emitter.error(e)
      }
    }
      .timeout(Duration.ofSeconds(60))
      .next()
  }

  fun handleScrapeResponse(corrId: String, harvestJobId: String, scrapeResponse: ScrapeResponseInput) {
    log.info("[$corrId] handleScrapeResponse $harvestJobId, err=${scrapeResponse.errorMessage}")
    pendingJobs[harvestJobId]?.let {
      if (scrapeResponse.failed) {
        it.error(IllegalArgumentException(StringUtils.trimToEmpty(scrapeResponse.errorMessage)))
      } else {
        it.next(AgentResponse(JsonUtil.gson.toJson(scrapeResponse.fromDto())))
      }
      pendingJobs.remove(harvestJobId)
    } ?: log.error("[$corrId] emitter for job ID not found (${pendingJobs.size} pending jobs)")
  }

  fun findAll(userId: UUID?): List<AgentEntity> {
    return agentDAO.findAllByOwnerIdOrOpenInstanceIsTrue(userId)
  }

  fun agentRefs(): ArrayList<AgentRef> {
    return agentRefs
  }
}

data class AgentRef(
  val secretKeyId: UUID,
  val ownerId: UUID,
  val name: String,
  val version: String,
  val connectionId: String,
  val os: OsInfo,
  val addedAt: Date,
  val emitter: FluxSink<AgentEvent>
) {
  override fun toString(): String {
    return "AgentRef(connectionId=$connectionId, secretKeyId=$secretKeyId, name=$name, version='$version', os=$os)"
  }
}
