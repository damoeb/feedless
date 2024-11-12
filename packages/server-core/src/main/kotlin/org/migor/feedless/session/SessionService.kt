package org.migor.feedless.session

import com.netflix.graphql.dgs.context.DgsContext
import graphql.schema.DataFetchingEnvironment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import org.apache.commons.lang3.StringUtils
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.config.DgsCustomContext
import org.migor.feedless.data.jpa.enums.Vertical
import org.migor.feedless.user.UserDAO
import org.migor.feedless.user.UserEntity
import org.migor.feedless.user.userIdOptional
import org.migor.feedless.util.CryptUtil.newCorrId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder
import java.util.*
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

fun injectCurrentUser(currentCoroutineContext: CoroutineContext, dfe: DataFetchingEnvironment): RequestContext {
  val requestContext = currentCoroutineContext[RequestContext] ?: createRequestContext()
  DgsContext.getCustomContext<DgsCustomContext>(dfe).userId = requestContext.userId
  return requestContext
}

fun createRequestContext(): RequestContext {
  val context = RequestContext(corrId = newCorrId())

  runCatching {
    context.userId = if (SecurityContextHolder.getContext().authentication is OAuth2AuthenticationToken) {
      StringUtils.trimToNull((SecurityContextHolder.getContext().authentication as OAuth2AuthenticationToken).principal.attributes[JwtParameterNames.USER_ID] as String)
        ?.let { UUID.fromString(it) }
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
    var userId: UUID? = null
) : AbstractCoroutineContextElement(RequestContext) {
  companion object Key : CoroutineContext.Key<RequestContext>
}

@Service
@Profile("${AppProfiles.session} & ${AppLayer.service}")
@Transactional
class SessionService {

  @Autowired
  private lateinit var userDAO: UserDAO

  suspend fun isUser(): Boolean = StringUtils.isNotBlank(coroutineContext[RequestContext]!!.userId?.toString())

  suspend fun user(): UserEntity {
    val notFoundException = IllegalArgumentException("user not found")
    return coroutineContext.userIdOptional()
      ?.let { withContext(Dispatchers.IO) { userDAO.findById(it).orElseThrow { notFoundException } } }
      ?: throw notFoundException
  }

  suspend fun activeProductFromRequest(): Vertical? {
    return currentCoroutineContext()[RequestContext]?.product
  }

  @Deprecated("")
  suspend fun userId(): UUID? = currentCoroutineContext()[RequestContext]?.userId
}
