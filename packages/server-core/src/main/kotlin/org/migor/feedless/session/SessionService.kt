package org.migor.feedless.session

import com.netflix.graphql.dgs.context.DgsContext
import graphql.schema.DataFetchingEnvironment
import kotlinx.coroutines.currentCoroutineContext
import org.apache.commons.lang3.StringUtils
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.Vertical
import org.migor.feedless.capability.UserCapability
import org.migor.feedless.config.DgsCustomContext
import org.migor.feedless.user.UserEntity
import org.migor.feedless.user.UserId
import org.migor.feedless.user.userIdOptional
import org.migor.feedless.util.CryptUtil.newCorrId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

fun injectCurrentUser(currentCoroutineContext: CoroutineContext, dfe: DataFetchingEnvironment): RequestContext {
  val requestContext = currentCoroutineContext[RequestContext] ?: createRequestContext()
  DgsContext.getCustomContext<DgsCustomContext>(dfe).userId = requestContext.userId
  return requestContext
}

private fun OAuth2AuthenticationToken.getUserCapability(): UserCapability? {
  return this.authorities.find { it.authority == UserCapability.ID }
    ?.let { UserCapability.fromString((it as LazyGrantedAuthority).getPayload()) }
}

fun createRequestContext(): RequestContext {
  val context = RequestContext(corrId = newCorrId())

  runCatching {
    context.userId = if (SecurityContextHolder.getContext().authentication is OAuth2AuthenticationToken) {
      (SecurityContextHolder.getContext().authentication as OAuth2AuthenticationToken).getUserCapability()?.userId
    } else {
      null
    }

    context.product = runCatching {
      RequestContextHolder.currentRequestAttributes().getAttribute("product", RequestAttributes.SCOPE_REQUEST)
        ?.let {
          Vertical.valueOf(it as String)
        }
    }.getOrNull()

  }.onFailure {
    println(it.message)
  }
  return context
}

class RequestContext(
  var product: Vertical? = null,
  val corrId: String? = newCorrId(),
  var userId: UserId? = null
) : AbstractCoroutineContextElement(RequestContext) {
  companion object Key : CoroutineContext.Key<RequestContext>
}

@Service
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.session} & ${AppLayer.service}")
class SessionService {

  @Autowired
  private lateinit var authService: AuthService

  suspend fun isUser(): Boolean = StringUtils.isNotBlank(coroutineContext[RequestContext]!!.userId?.toString())

  @Transactional(readOnly = true)
  suspend fun user(): UserEntity {
    val notFoundException = IllegalArgumentException("user not found")
    return coroutineContext.userIdOptional()
      ?.let { authService.findUserById(it) ?: throw notFoundException }
      ?: throw notFoundException
  }

  suspend fun activeProductFromRequest(): Vertical? {
    return currentCoroutineContext()[RequestContext]?.product
  }

  @Deprecated("")
  suspend fun userId(): UserId? = currentCoroutineContext()[RequestContext]?.userId
}
