package org.migor.feedless.agent

import io.micrometer.core.instrument.MeterRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.lang3.StringUtils
import org.migor.feedless.AppLayer
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
import org.migor.feedless.session.RequestContext
import org.migor.feedless.session.TokenProvider
import org.migor.feedless.source.SourceEntity
import org.migor.feedless.source.toDto
import org.migor.feedless.user.corrId
import org.migor.feedless.util.JsonUtil
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink
import reactor.core.publisher.Mono
import java.io.Serializable
import java.time.Duration
import java.time.LocalDateTime
import java.util.*
import kotlin.coroutines.coroutineContext

class AgentResponse(private val scrapeResponse: String) : Serializable {

  fun get(): ScrapeResponse {
    return JsonUtil.gson.fromJson(scrapeResponse, ScrapeResponse::class.java)
  }
}

@Service
@Profile("${AppProfiles.agent} & ${AppLayer.service}")
class AgentService(
  private val userSecretService: UserSecretService,
  private val tokenProvider: TokenProvider,
  private val agentDAO: AgentDAO,
  private val meterRegistry: MeterRegistry
) {
  private val log = LoggerFactory.getLogger(AgentService::class.simpleName)
  private val agentRefs: ArrayList<AgentRef> = ArrayList()
  private val pendingJobs: MutableMap<String, FluxSink<AgentResponse>> = mutableMapOf()

  suspend fun registerAgent(data: RegisterAgentInput): Publisher<AgentEvent> {
    return Flux.create { emitter ->
      CoroutineScope(RequestContext()).launch {
        userSecretService.findBySecretKeyValue(data.secretKey.secretKey, data.secretKey.email)
          ?.let { securityKey ->
            val now = LocalDateTime.now()
            if (securityKey.validUntil.isBefore(now)) {
              emitter.error(IllegalAccessException("Key is expired"))
              emitter.complete()
            } else {
              userSecretService.updateLastUsed(securityKey.id, now)
              val agentRef =
                AgentRef(
                  securityKey.id,
                  securityKey.ownerId,
                  data.name,
                  data.version,
                  data.connectionId,
                  data.os,
                  now,
                  emitter
                )

              emitter.onDispose {
                CoroutineScope(Dispatchers.Default).launch {
                  removeAgent(agentRef)
                }
              }
              emitter.next(
                AgentEvent(
                  corrId = "corrId",
                  callbackId = "none",
                  authentication = AgentAuthentication(
                    token = tokenProvider.createJwtForAgent(securityKey).tokenValue
                  )
                )
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
  }

  private suspend fun addAgent(agentRef: AgentRef) {
    val corrId = coroutineContext.corrId()
    agentRefs.add(agentRef)

    log.info("[$corrId] Added Agent $agentRef")

    withContext(Dispatchers.IO) {
      agentDAO.findByConnectionIdAndSecretKeyId(agentRef.connectionId, agentRef.secretKeyId)?.let {
        agentDAO.delete(it)
      }

      val agent = AgentEntity()
      agent.secretKeyId = agentRef.secretKeyId
      agent.name = agentRef.name
      agent.version = agentRef.version
      agent.lastSyncedAt = LocalDateTime.now()
      agent.connectionId = agentRef.connectionId
      agent.ownerId = agentRef.ownerId
      agent.openInstance = true
      agentDAO.save(agent)
    }

    meterRegistry.gauge(AppMetrics.agentCounter, 0)?.inc()
  }

  private suspend fun removeAgent(agentRef: AgentRef) {
    val corrId = coroutineContext.corrId()
    agentRefs.remove(agentRef)
    log.info("[$corrId] Removing Agent by connectionId=${agentRef.connectionId} and secretKeyId=${agentRef.secretKeyId}")
    withContext(Dispatchers.IO) {
      agentDAO.findByConnectionIdAndSecretKeyId(agentRef.connectionId, agentRef.secretKeyId)?.let {
        agentDAO.delete(it)
      }
    }
    meterRegistry.gauge(AppMetrics.agentCounter, 0)?.dec()
  }

  fun hasAgents(): Boolean = agentRefs.isNotEmpty()

  //  @Cacheable(value = [CacheNames.AGENT_RESPONSE], keyGenerator = "agentResponseCacheKeyGenerator")
  suspend fun prerender(source: SourceEntity): AgentResponse {
    val corrId = coroutineContext.corrId()
    return if (hasAgents()) {
      val agentRef = agentRefs[(Math.random() * agentRefs.size).toInt()]
      prerenderWithAgent(source, agentRef)
        .toFuture()
        .await()
    } else {
      log.warn("[$corrId] no agents present")
      throw ResumableHarvestException(corrId!!, "No agents available", Duration.ofMinutes(10))
    }
  }

  private suspend fun prerenderWithAgent(
    source: SourceEntity,
    agentRef: AgentRef
  ): Mono<AgentResponse> {
    val corrId = coroutineContext.corrId()
    log.debug("[$corrId] preparing")
    return Flux.create { emitter ->
      try {
        val harvestJobId = UUID.randomUUID().toString()
        agentRef.emitter.next(
          AgentEvent(
            callbackId = harvestJobId,
            corrId = corrId,
            scrape = source.toDto()
          )
        )
        log.debug("[$corrId] submitted agent job $harvestJobId")
        pendingJobs[harvestJobId] = emitter
      } catch (e: Exception) {
        log.error("$corrId] prerenderWithAgent failed: ${e.message}", e)
        emitter.error(e)
      }
    }
      .timeout(Duration.ofSeconds(60))
      .next()
  }

  suspend fun handleScrapeResponse(harvestJobId: String, scrapeResponse: ScrapeResponseInput) {
    val corrId = coroutineContext.corrId()
    log.info("[$corrId] handleScrapeResponse $harvestJobId, err=${scrapeResponse.errorMessage}")
    pendingJobs[harvestJobId]?.let {
      if (scrapeResponse.ok) {
        it.next(AgentResponse(JsonUtil.gson.toJson(scrapeResponse.fromDto())))
      } else {
        it.error(IllegalArgumentException(StringUtils.trimToEmpty(scrapeResponse.errorMessage)))
      }
      pendingJobs.remove(harvestJobId)
    } ?: log.error("[$corrId] emitter for job ID not found (${pendingJobs.size} pending jobs)")
  }

  suspend fun findAllByUserId(userId: UUID?): List<AgentEntity> {
    return withContext(Dispatchers.IO) {
      agentDAO.findAllByOwnerIdOrOpenInstanceIsTrue(userId)
    }
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
  val addedAt: LocalDateTime,
  val emitter: FluxSink<AgentEvent>
) {
  override fun toString(): String {
    return "AgentRef(connectionId=$connectionId, secretKeyId=$secretKeyId, name=$name, version='$version', os=$os)"
  }
}
