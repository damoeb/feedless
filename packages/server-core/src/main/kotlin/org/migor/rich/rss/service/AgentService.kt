package org.migor.rich.rss.service

import org.apache.commons.lang3.StringUtils
import org.migor.rich.rss.auth.TokenProvider
import org.migor.rich.rss.data.jpa.repositories.SecretKeyDAO
import org.migor.rich.rss.generated.types.AgentAuthentication
import org.migor.rich.rss.generated.types.AgentEvent
import org.migor.rich.rss.generated.types.HarvestRequest
import org.migor.rich.rss.generated.types.HarvestResponse
import org.migor.rich.rss.graphql.DtoResolver
import org.migor.rich.rss.harvest.PuppeteerHttpResponse
import org.migor.rich.rss.harvest.PuppeteerService
import org.migor.rich.rss.transform.GenericFeedFetchOptions
import org.migor.rich.rss.util.CryptUtil.newCorrId
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink
import java.time.Duration
import java.util.*

@Service
class AgentService: PuppeteerService {
  private val log = LoggerFactory.getLogger(AgentService::class.simpleName)
  private val agentRefs: ArrayList<AgentRef> = ArrayList()
  private val pendingJobs: MutableMap<String, FluxSink<PuppeteerHttpResponse>> = mutableMapOf()

  @Autowired
  lateinit var secretKeyDAO: SecretKeyDAO

  @Autowired
  lateinit var tokenProvider: TokenProvider

  @Autowired
  lateinit var propertyService: PropertyService

  fun registerAgent(email: String, secretKeyValue: String): Publisher<AgentEvent> {
    return Flux.create { emitter ->
      run {
        val secretKeyOptional = secretKeyDAO.findBySecretKeyValue(secretKeyValue, email)
        secretKeyOptional.ifPresentOrElse(
          {securityKey ->
            if (securityKey.validUntil.before(Date())) {
              emitter.error(IllegalAccessException("Key is expired"))
              emitter.complete()
            } else {
              secretKeyDAO.updateLastUsed(securityKey.id, Date())
              val agentRef = AgentRef(UUID.randomUUID(), emitter)

              emitter.onDispose {
                removeAgent(agentRef)
              }
              emitter.next(AgentEvent.newBuilder()
                .authentication(AgentAuthentication.newBuilder()
                  .token(tokenProvider.createJwtForAgent(securityKey).tokenValue)
                  .build()
                ).build())

              prerenderWithAgent(newCorrId(), GenericFeedFetchOptions(
                websiteUrl = "https://example.org",
                prerender = true,
                prerenderScript = ""
              ), agentRef)
                .timeout(Duration.ofSeconds(30))
                .take(1)
                .subscribe {
                  if (it.isError) {
                    emitter.error(IllegalAccessException("rendering failed"))
                    emitter.complete()
                  } else {
                    addAgent(agentRef)
                  }
                }
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

  override fun prerender(corrId: String, options: GenericFeedFetchOptions): Flux<PuppeteerHttpResponse> {
    return if (canPrerender()) {
      val agentRef = agentRefs[(Math.random() * agentRefs.size).toInt()]
      prerenderWithAgent(corrId, options, agentRef)
    } else {
      Flux.error(IllegalStateException("No agents available"))
    }
  }
  private fun prerenderWithAgent(corrId: String, options: GenericFeedFetchOptions, agentRef: AgentRef): Flux<PuppeteerHttpResponse> {
    return Flux.create { emitter -> run {
      val harvestJobId = UUID.randomUUID().toString()
      agentRef.emitter.next(AgentEvent.newBuilder()
        .harvestRequest(
          HarvestRequest.newBuilder()
            .id(harvestJobId)
            .corrId(corrId)
            .websiteUrl(options.websiteUrl)
            .prerender(options.prerender)
            .prerenderWaitUntil(DtoResolver.toDTO(options.prerenderWaitUntil))
            .prerenderScript(options.prerenderScript)
          .build())
        .build())

      log.info("$corrId] trigger agent job $harvestJobId")
      pendingJobs[harvestJobId] = emitter
    }
    }
  }

  fun handleAgentResponse(corrId: String, harvestJobId: String, harvestResponse: HarvestResponse) {
    log.info("[$corrId] handleAgentResponse $harvestJobId")
    pendingJobs[harvestJobId]?.let {
      it.next(PuppeteerHttpResponse(
        html = StringUtils.trimToEmpty(harvestResponse.html),
        url = harvestResponse.url,
        errorMessage = harvestResponse.errorMessage,
        isError = harvestResponse.isError,
      ))
      pendingJobs.remove(harvestJobId)
    } ?: log.error("[$corrId] emitter for job ID not found (${pendingJobs.size} pending jobs)")
  }

}

data class AgentRef(
  val agentId: UUID,
  val emitter: FluxSink<AgentEvent>,
)
