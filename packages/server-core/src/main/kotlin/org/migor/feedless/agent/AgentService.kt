package org.migor.feedless.agent

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
import org.migor.feedless.generated.types.AgentAuthentication
import org.migor.feedless.generated.types.AgentEvent
import org.migor.feedless.generated.types.RegisterAgentInput
import org.migor.feedless.generated.types.ScrapeResponseInput
import org.migor.feedless.session.AuthService
import org.migor.feedless.session.JwtTokenIssuer
import org.migor.feedless.session.RequestContext
import org.migor.feedless.source.Source
import org.migor.feedless.user.UserId
import org.migor.feedless.user.corrId
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink
import reactor.core.publisher.Mono
import java.time.Duration
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.coroutineContext


@Service
@Transactional(propagation = Propagation.NEVER)
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
    private val pendingJobs: MutableMap<String, FluxSink<AgentResponse>> = mutableMapOf()
    private val agentCounter = AtomicInteger(0)

    @PostConstruct
    fun postConstruct() {
        meterRegistry.gauge(AppMetrics.agentCounter, agentCounter)
    }

    suspend fun registerAgent(data: RegisterAgentInput): Publisher<AgentEvent> {
        return Flux.create { emitter ->
            CoroutineScope(RequestContext()).launch {
                authService.findBySecretKeyValue(data.secretKey.secretKey, data.secretKey.email)
                    ?.let { securityKey ->
                        val now = LocalDateTime.now()
                        if (securityKey.validUntil.isBefore(now)) {
                            emitter.error(IllegalAccessException("Key is expired"))
                            emitter.complete()
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
                                    emitter
                                )

                            emitter.onDispose {
                                CoroutineScope(Dispatchers.Default).launch {
                                    context.getBean(AgentService::class.java).removeAgent(agentRef)
                                }
                            }
                            emitter.next(
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
                        emitter.error(IllegalAccessException("user/key combination not found or account locked"))
                        emitter.complete()
                    }
            }
        }
    }

    suspend fun hasAgents(): Boolean = agentRefs.isNotEmpty()

    //  @Cacheable(value = [CacheNames.AGENT_RESPONSE], keyGenerator = "agentResponseCacheKeyGenerator")
    suspend fun prerender(source: Source): AgentResponse {
        val corrId = coroutineContext.corrId()
        return if (hasAgents()) {
            val agentRef = agentRefs[(Math.random() * agentRefs.size).toInt()]
            prerenderWithAgent(source, agentRef)
                .toFuture()
                .await()
        } else {
            log.warn("[$corrId] no agents present")
            throw ResumableHarvestException("No agents available", Duration.ofMinutes(10))
        }
    }

    suspend fun handleScrapeResponse(harvestJobId: String, scrapeResponse: ScrapeResponseInput) {
        val corrId = coroutineContext.corrId()
        log.info("[$corrId] handleScrapeResponse $harvestJobId, err=${scrapeResponse.errorMessage}")
        pendingJobs[harvestJobId]?.let {
            if (scrapeResponse.ok) {
                it.next(AgentResponse(Gson().toJson(scrapeResponse.fromDto())))
            } else {
                it.error(IllegalArgumentException(StringUtils.trimToEmpty(scrapeResponse.errorMessage)))
            }
            pendingJobs.remove(harvestJobId)
        } ?: log.error("[$corrId] emitter for job ID not found (${pendingJobs.size} pending jobs)")
    }

    @Transactional(readOnly = true)
    suspend fun findAllByUserId(userId: UserId?): List<Agent> {
        return agentRegistry.findAllByOwnerIdOrOpenInstanceIsTrue(userId)
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    fun agentRefs(): ArrayList<AgentRef> {
        return agentRefs
    }

    private suspend fun prerenderWithAgent(
        source: Source,
        agentRef: AgentRef
    ): Mono<AgentResponse> {
        val corrId = coroutineContext.corrId()
        log.debug("[$corrId] preparing")
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
                log.info("[$corrId] submitted agent job $agentJobId")
                pendingJobs[agentJobId] = emitter
            } catch (e: Exception) {
                log.error("$corrId] prerenderWithAgent failed: ${e.message}", e)
                emitter.error(e)
            }
        }
            .timeout(Duration.ofSeconds(60))
            .next()
    }

    @Transactional
    suspend fun addAgent(agentRef: AgentRef) {
        val corrId = coroutineContext.corrId()
        agentRefs.add(agentRef)

        log.info("[$corrId] Added Agent $agentRef")

        withContext(Dispatchers.IO) {
            agentRegistry.findByConnectionIdAndSecretKeyId(agentRef.connectionId, agentRef.secretKeyId)?.let {
                agentRegistry.delete(it)
            }

            val agent = Agent(
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
            agentRegistry.save(agent)
        }

        agentCounter.incrementAndGet();
    }

    @Transactional
    suspend fun removeAgent(agentRef: AgentRef) {
        val corrId = coroutineContext.corrId()
        agentRefs.remove(agentRef)
        log.info("[$corrId] Removing Agent by connectionId=${agentRef.connectionId} and secretKeyId=${agentRef.secretKeyId}")
        withContext(Dispatchers.IO) {
            agentRegistry.findByConnectionIdAndSecretKeyId(agentRef.connectionId, agentRef.secretKeyId)?.let {
                agentRegistry.delete(it)
            }
        }
        agentCounter.decrementAndGet();
    }
}

