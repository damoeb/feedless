package org.migor.feedless.service

import org.migor.feedless.api.auth.TokenProvider
import org.migor.feedless.api.graphql.DtoResolver.toDTO
import org.migor.feedless.config.CacheNames
import org.migor.feedless.generated.types.AgentAuthentication
import org.migor.feedless.generated.types.AgentEvent
import org.migor.feedless.generated.types.HarvestRequest
import org.migor.feedless.generated.types.HarvestResponse
import org.migor.feedless.harvest.ResumableHarvestException
import org.migor.feedless.web.FetchOptions
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink
import reactor.core.publisher.Mono
import java.time.Duration
import java.util.*

@Service
class AgentService: PuppeteerService {
  private val log = LoggerFactory.getLogger(AgentService::class.simpleName)
  private val agentRefs: ArrayList<AgentRef> = ArrayList()
  private val pendingJobs: MutableMap<String, FluxSink<PuppeteerHttpResponse>> = mutableMapOf()

  @Autowired
  lateinit var userSecretService: UserSecretService

  @Autowired
  lateinit var tokenProvider: TokenProvider

  @Autowired
  lateinit var propertyService: PropertyService

  fun registerPrerenderAgent(email: String, secretKeyValue: String): Publisher<AgentEvent> {
    return Flux.create { emitter ->
      run {
        val secretKeyOptional = userSecretService.findBySecretKeyValue(secretKeyValue, email)
        secretKeyOptional.ifPresentOrElse(
          {securityKey ->
            if (securityKey.validUntil.before(Date())) {
              emitter.error(IllegalAccessException("Key is expired"))
              emitter.complete()
            } else {
              userSecretService.updateLastUsed(securityKey.id, Date())
              val agentRef = AgentRef(UUID.randomUUID(), emitter)

              emitter.onDispose {
                removeAgent(agentRef)
              }
              emitter.next(AgentEvent.newBuilder()
                .authentication(AgentAuthentication.newBuilder()
                  .token(tokenProvider.createJwtForAgent(securityKey).tokenValue)
                  .build()
                ).build())

//              fremd test
//              prerenderWithAgent(newCorrId(), FetchOptions(
//                websiteUrl = "https://example.org",
//                prerender = true,
//                prerenderScript = ""
//              ), agentRef)
//                .timeout(Duration.ofSeconds(30))
//                .take(1)
//                .subscribe {
//                  if (it.isError) {
//                    emitter.error(IllegalAccessException("rendering failed"))
//                    emitter.complete()
//                  } else {
                    addAgent(agentRef)
//                  }
//                }
            }
          },
          {
            emitter.error(IllegalAccessException("user/key combination not found or account locked"))
            emitter.complete()
          }
        )
      }
    }
  }

  private fun addAgent(agentRef: AgentRef) {
    agentRefs.add(agentRef)
    log.info("Added Agent (${agentRefs.size})")
  }

  private fun removeAgent(agentRef: AgentRef) {
    agentRefs.remove(agentRef)
    log.info("Removed Agent (${agentRefs.size})")
  }

  override fun canPrerender(): Boolean = agentRefs.isNotEmpty()

  override fun prerender(corrId: String, options: FetchOptions): Mono<PuppeteerHttpResponse> {
    return if (canPrerender()) {
      val agentRef = agentRefs[(Math.random() * agentRefs.size).toInt()]
      prerenderWithAgent(corrId, options, agentRef)
    } else {
      Mono.error(ResumableHarvestException("No agents available", Duration.ofMinutes(10)))
    }
  }
  private fun prerenderWithAgent(corrId: String, options: FetchOptions, agentRef: AgentRef): Mono<PuppeteerHttpResponse> {
    return Flux.create { emitter ->
      run {
        val harvestJobId = UUID.randomUUID().toString()
        agentRef.emitter.next(
          AgentEvent.newBuilder()
            .harvestRequest(
              HarvestRequest.newBuilder()
                .id(harvestJobId)
                .corrId(corrId)
                .websiteUrl(options.websiteUrl)
                .baseXpath(options.baseXpath)
                .emit(toDTO(options.emit))
                .prerender(options.prerender)
                .prerenderWaitUntil(toDTO(options.prerenderWaitUntil))
                .prerenderScript(options.prerenderScript)
                .build()
            )
            .build()
        )
        log.info("$corrId] trigger agent job $harvestJobId")
        pendingJobs[harvestJobId] = emitter
      }
    }
      .timeout(Duration.ofSeconds(60))
      .next()
  }

  fun handleAgentResponse(corrId: String, harvestJobId: String, harvestResponse: HarvestResponse) {
    log.info("[$corrId] handleAgentResponse $harvestJobId, err=${harvestResponse.isError}")
    pendingJobs[harvestJobId]?.let {
      it.next(
        PuppeteerHttpResponse(
        dataBase64 = harvestResponse.dataBase64,
        dataAscii = harvestResponse.dataAscii,
        url = harvestResponse.url,
        errorMessage = harvestResponse.errorMessage,
        isError = harvestResponse.isError,
      )
      )
      pendingJobs.remove(harvestJobId)
    } ?: log.error("[$corrId] emitter for job ID not found (${pendingJobs.size} pending jobs)")
  }

}

data class AgentRef(
  val agentId: UUID,
  val emitter: FluxSink<AgentEvent>,
)
