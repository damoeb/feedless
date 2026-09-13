package org.migor.feedless.browserautomation

import com.google.gson.Gson
import io.micrometer.core.instrument.MeterRegistry
import jakarta.annotation.PostConstruct
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
import org.migor.feedless.api.mapper.toDto
import org.migor.feedless.capability.RequestContext
import org.migor.feedless.capability.currentCorrId
import org.migor.feedless.generated.types.AgentAuthentication
import org.migor.feedless.generated.types.AgentEvent
import org.migor.feedless.generated.types.RegisterAgentInput
import org.migor.feedless.generated.types.ScrapeResponseInput
import org.migor.feedless.session.AuthService
import org.migor.feedless.session.JwtTokenIssuer
import org.migor.feedless.source.Source
import org.migor.feedless.user.UserId
import org.migor.feedless.util.CryptUtil.newCorrId
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink
import reactor.core.publisher.Mono
import java.time.Duration
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.atomic.AtomicInteger


@Service
@Profile("${AppProfiles.browserAutomation} & ${AppLayer.service}")
class BrowserAutomationService(
  private val authService: AuthService,
  private val jwtTokenIssuer: JwtTokenIssuer,
  private val browserAutomationRegistry: BrowserAutomationRegistry,
  private val meterRegistry: MeterRegistry,
  private val context: ApplicationContext
) : BrowserAutomationGateway {
  private val log = LoggerFactory.getLogger(BrowserAutomationService::class.simpleName)
  private val agentRefs: ArrayList<BrowserAutomationRef> = ArrayList()
  private val pendingJobs: MutableMap<String, FluxSink<BrowserAutomationResponse>> = mutableMapOf()
  private val agentCounter = AtomicInteger(0)

  @PostConstruct
  fun postConstruct() {
    meterRegistry.gauge(AppMetrics.agentCounter, agentCounter)
  }

  override suspend fun registerAgent(data: RegisterAgentInput): Publisher<AgentEvent> {
    val requestContext = RequestContext(corrId = currentCorrId() ?: newCorrId())
    return Flux.create { emitter ->
      CoroutineScope(requestContext).launch {
        authService.findBySecretKeyValue(data.secretKey.secretKey, data.secretKey.email)
          ?.let { securityKey ->
            val now = LocalDateTime.now()
            if (securityKey.validUntil.isBefore(now)) {
              emitter.error(IllegalAccessException("Key is expired"))
              emitter.complete()
            } else {
              authService.updateLastUsed(securityKey.id, now)
              val agentRef =
                BrowserAutomationRef(
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
                  context.getBean(BrowserAutomationService::class.java).removeAgent(agentRef)
                }
              }
              emitter.next(
                AgentEvent(
                  corrId = requestContext.corrId,
                  callbackId = "none",
                  authentication = AgentAuthentication(
                    token = jwtTokenIssuer.createJwtForService(securityKey).tokenValue
                  )
                )
              )
              context.getBean(BrowserAutomationService::class.java).addAgent(agentRef)
            }
          }
          ?: run {
            emitter.error(IllegalAccessException("user/key combination not found or account locked"))
            emitter.complete()
          }
      }
    }
  }

  suspend fun hasAgents(): Boolean = agentRefs.isNotEmpty()

  //  @Cacheable(value = [CacheNames.AGENT_RESPONSE], keyGenerator = "agentResponseCacheKeyGenerator")
  suspend fun prerender(source: Source): BrowserAutomationResponse {
    return if (hasAgents()) {
      val agentRef = agentRefs[(Math.random() * agentRefs.size).toInt()]
      prerenderWithAgent(source, agentRef)
        .toFuture()
        .await()
    } else {
      log.warn("no agents present")
      throw ResumableHarvestException("No agents available", Duration.ofMinutes(10))
    }
  }

  override suspend fun handleScrapeResponse(harvestJobId: String, scrapeResponse: ScrapeResponseInput) {
    log.info("handleScrapeResponse $harvestJobId, err=${scrapeResponse.errorMessage}")
    pendingJobs[harvestJobId]?.let {
      if (scrapeResponse.ok) {
        it.next(BrowserAutomationResponse(Gson().toJson(scrapeResponse.fromDto())))
      } else {
        it.error(IllegalArgumentException(StringUtils.trimToEmpty(scrapeResponse.errorMessage)))
      }
      pendingJobs.remove(harvestJobId)
    } ?: log.error("emitter for job ID not found (${pendingJobs.size} pending jobs)")
  }

  fun agentRefs(): ArrayList<BrowserAutomationRef> {
    return agentRefs
  }

  private suspend fun prerenderWithAgent(
    source: Source,
    agentRef: BrowserAutomationRef
  ): Mono<BrowserAutomationResponse> {
    log.debug("preparing")
    // The worker logs under this id, so its lines trace back to the harvest that asked.
    val corrId = currentCorrId() ?: newCorrId()
    return Flux.create { emitter ->
      try {
        val agentJobId = UUID.randomUUID().toString()
        agentRef.emitter.next(
          AgentEvent(
            callbackId = agentJobId,
            corrId = corrId,
            scrape = source.toDto()
          )
        )
        log.info("submitted agent job $agentJobId")
        pendingJobs[agentJobId] = emitter
      } catch (e: Exception) {
        log.error("$corrId] prerenderWithAgent failed: ${e.message}", e)
        emitter.error(e)
      }
    }
      .timeout(Duration.ofSeconds(60))
      .next()
  }

  suspend fun addAgent(agentRef: BrowserAutomationRef) = withContext(Dispatchers.IO) {
    log.info("Adding Agent $agentRef")

    agentRefs.add(agentRef)


    browserAutomationRegistry.findByConnectionIdAndSecretKeyId(agentRef.connectionId, agentRef.secretKeyId)?.let {
      browserAutomationRegistry.delete(it)
    }

    browserAutomationRegistry.save(
      BrowserAutomation(
        id = BrowserAutomationId(UUID.randomUUID()),
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

  suspend fun removeAgent(agentRef: BrowserAutomationRef) = withContext(Dispatchers.IO) {
    agentRefs.remove(agentRef)
    log.info("Removing Agent by connectionId=${agentRef.connectionId} and secretKeyId=${agentRef.secretKeyId}")
    browserAutomationRegistry.findByConnectionIdAndSecretKeyId(agentRef.connectionId, agentRef.secretKeyId)?.let {
      browserAutomationRegistry.delete(it)
    }

    agentCounter.decrementAndGet();
  }
}

