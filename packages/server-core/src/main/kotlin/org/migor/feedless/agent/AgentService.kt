package org.migor.feedless.agent

import com.google.gson.Gson
import io.micrometer.core.instrument.MeterRegistry
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.apache.commons.lang3.StringUtils
import org.migor.feedless.AppLayer
import org.migor.feedless.AppMetrics
import org.migor.feedless.AppProfiles
import org.migor.feedless.ResumableHarvestException
import org.migor.feedless.api.fromDto
import org.migor.feedless.api.mapper.toDto
import org.migor.feedless.generated.types.AgentAuthentication
import org.migor.feedless.generated.types.AgentEvent
import org.migor.feedless.generated.types.RegisterAgentInput
import org.migor.feedless.generated.types.ScrapeResponseInput
import org.migor.feedless.session.AuthService
import org.migor.feedless.session.JwtTokenIssuer
import org.migor.feedless.source.Source
import org.migor.feedless.user.UserId
import org.migor.feedless.user.corrId
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.cancellation.CancellationException


@Service
@Profile("${AppProfiles.agent} & ${AppLayer.service}")
class AgentService(
  private val authService: AuthService,
  private val jwtTokenIssuer: JwtTokenIssuer,
  private val agentRegistry: AgentRegistry,
  private val meterRegistry: MeterRegistry,
  private val context: ApplicationContext
) {
  private val log = LoggerFactory.getLogger(AgentService::class.simpleName)
  private val agentRefs: ArrayList<AgentRef> = ArrayList()
  private val pendingJobs: MutableMap<String, CompletableDeferred<AgentResponse>> = mutableMapOf()
  private val agentCounter = AtomicInteger(0)

  @PostConstruct
  fun postConstruct() {
    meterRegistry.gauge(AppMetrics.agentCounter, agentCounter)
  }

  suspend fun registerAgent(data: RegisterAgentInput): Channel<AgentEvent> = withContext(Dispatchers.IO) {
    val channel = Channel<AgentEvent>()

    authService.findBySecretKeyValue(data.secretKey.secretKey, data.secretKey.email)
      ?.let { securityKey ->
        val now = LocalDateTime.now()
        if (securityKey.validUntil.isBefore(now)) {
          channel.cancel(CancellationException("Key is expired"))
//          emitter.complete()
        } else {
          authService.updateLastUsed(securityKey.id, now)
          val agentRef =
            AgentRef(
              securityKey.id,
              securityKey.ownerId,
              data.name,
              data.version,
              data.connectionId,
              data.os,
              now,
              channel
            )

          channel.invokeOnClose {
            CoroutineScope(Dispatchers.Default).launch {
              context.getBean(AgentService::class.java).removeAgent(agentRef)
            }
          }
          channel.send(
            AgentEvent(
              corrId = "corrId",
              callbackId = "none",
              authentication = AgentAuthentication(
                token = jwtTokenIssuer.createJwtForService(securityKey).tokenValue
              )
            )
          )
          context.getBean(AgentService::class.java).addAgent(agentRef)
        }
      }
      ?: run {
        channel.cancel(CancellationException("user/key combination not found or account locked"))
//        emitter.complete()
      }


    channel
  }

  suspend fun hasAgents(): Boolean = agentRefs.isNotEmpty()

  //  @Cacheable(value = [CacheNames.AGENT_RESPONSE], keyGenerator = "agentResponseCacheKeyGenerator")
  suspend fun prerender(source: Source): AgentResponse {
    val corrId = currentCoroutineContext().corrId()
    return if (hasAgents()) {
      val agentRef = agentRefs[(Math.random() * agentRefs.size).toInt()]
      prerenderWithAgent(source, agentRef)
        .await()
    } else {
      log.warn("[$corrId] no agents present")
      throw ResumableHarvestException("No agents available", Duration.ofMinutes(10))
    }
  }

  suspend fun handleScrapeResponse(harvestJobId: String, scrapeResponse: ScrapeResponseInput) {
    val corrId = currentCoroutineContext().corrId()
    log.info("[$corrId] handleScrapeResponse $harvestJobId, err=${scrapeResponse.errorMessage}")
    pendingJobs[harvestJobId]?.let {
      if (scrapeResponse.ok) {
        it.complete(AgentResponse(Gson().toJson(scrapeResponse.fromDto())))
      } else {
        it.cancel(CancellationException(StringUtils.trimToEmpty(scrapeResponse.errorMessage)))
      }
      pendingJobs.remove(harvestJobId)
    } ?: log.error("[$corrId] emitter for job ID not found (${pendingJobs.size} pending jobs)")
  }

  suspend fun findAllByUserId(userId: UserId?): List<Agent> = withContext(Dispatchers.IO) {
    agentRegistry.findAllByOwnerIdOrOpenInstanceIsTrue(userId)
  }

  fun agentRefs(): ArrayList<AgentRef> {
    return agentRefs
  }

  private suspend fun prerenderWithAgent(
    source: Source,
    agentRef: AgentRef
  ): CompletableDeferred<AgentResponse> {
    val corrId = currentCoroutineContext().corrId()
    log.debug("[$corrId] preparing")
    val deferred = CompletableDeferred<AgentResponse>()

    withTimeout(Duration.ofSeconds(60).toMillis()) {   // 5 seconds
      try {
        val agentJobId = UUID.randomUUID().toString()
        agentRef.emitter.send(
          AgentEvent(
            callbackId = agentJobId,
            corrId = corrId,
            scrape = source.toDto()
          )
        )
        log.info("[$corrId] submitted agent job $agentJobId")
        pendingJobs[agentJobId] = deferred
      } catch (e: Exception) {
        log.error("$corrId] prerenderWithAgent failed: ${e.message}", e)
        deferred.cancel(CancellationException(e))
      }
    }

    return deferred
  }

  suspend fun addAgent(agentRef: AgentRef) = withContext(Dispatchers.IO) {
    val corrId = coroutineContext.corrId()
    agentRefs.add(agentRef)

    log.info("[$corrId] Added Agent $agentRef")

    agentRegistry.findByConnectionIdAndSecretKeyId(agentRef.connectionId, agentRef.secretKeyId)?.let {
      agentRegistry.delete(it)
    }

    agentRegistry.save(
      Agent(
        id = AgentId(UUID.randomUUID()),
        secretKeyId = agentRef.secretKeyId,
        name = agentRef.name,
        version = agentRef.version,
        lastSyncedAt = LocalDateTime.now(),
        connectionId = agentRef.connectionId,
        ownerId = agentRef.ownerId,
        openInstance = true,
        createdAt = LocalDateTime.now(),
      )
    )

    agentCounter.incrementAndGet();
  }

  suspend fun removeAgent(agentRef: AgentRef) = withContext(Dispatchers.IO) {
    val corrId = coroutineContext.corrId()
    agentRefs.remove(agentRef)
    log.info("[$corrId] Removing Agent by connectionId=${agentRef.connectionId} and secretKeyId=${agentRef.secretKeyId}")
    agentRegistry.findByConnectionIdAndSecretKeyId(agentRef.connectionId, agentRef.secretKeyId)?.let {
      agentRegistry.delete(it)
    }

    agentCounter.decrementAndGet();
  }
}

