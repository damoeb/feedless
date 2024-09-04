package org.migor.feedless.session

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.reactor.ReactorContext
import kotlinx.coroutines.withContext
import org.apache.commons.lang3.StringUtils
import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.enums.ProductCategory
import org.migor.feedless.user.UserDAO
import org.migor.feedless.user.UserEntity
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

fun useRequestContext(currentCoroutineContext: CoroutineContext): RequestContextElement {
  val rctx = currentCoroutineContext[ReactorContext]
  return currentCoroutineContext[RequestContextElement] ?: createRequestContext()
}

fun createRequestContext(): RequestContextElement {
  val context = RequestContextElement()

  runCatching {
    context.userId = if (SecurityContextHolder.getContext().authentication is OAuth2AuthenticationToken) {
      StringUtils.trimToNull((SecurityContextHolder.getContext().authentication as OAuth2AuthenticationToken).principal.attributes[JwtParameterNames.USER_ID] as String)
    } else {
      null
    }

    context.product = runCatching {
      RequestContextHolder.currentRequestAttributes().getAttribute("product", RequestAttributes.SCOPE_REQUEST)
        ?.let {
          ProductCategory.valueOf(it as String)
        }
    }.getOrNull()

  }.onFailure {
    println(it.message)
  }
  return context
}

class RequestContextElement : AbstractCoroutineContextElement(RequestContextElement) {
  companion object Key : CoroutineContext.Key<RequestContextElement>

  var product: ProductCategory? = null
  var userId: String? = null
}

@Service
@Profile(AppProfiles.database)
@Transactional
class SessionService {

  @Autowired
  private lateinit var userDAO: UserDAO

  suspend fun isUser(): Boolean = StringUtils.isNotBlank(attr(JwtParameterNames.USER_ID))

  suspend fun user(corrId: String): UserEntity {
    val notFoundException = IllegalArgumentException("user not found ($corrId)")
    return userId()?.let { withContext(Dispatchers.IO) { userDAO.findById(it).orElseThrow { notFoundException } } }
      ?: throw notFoundException
  }

  suspend fun activeProductFromRequest(): ProductCategory? {
    return currentCoroutineContext()[RequestContextElement]?.product
  }

  suspend fun userId(): UUID? = attr(JwtParameterNames.USER_ID)?.let { UUID.fromString(it) }

  private suspend fun attr(param: String): String? {
    return currentCoroutineContext()[RequestContextElement]?.userId
  }
}
