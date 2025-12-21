package org.migor.feedless.session

import com.google.gson.JsonSyntaxException
import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kotlinx.coroutines.runBlocking
import org.apache.commons.lang3.StringUtils
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.Vertical
import org.migor.feedless.api.ApiParams
import org.migor.feedless.util.CryptUtil.newCorrId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes


@Component
@Deprecated("use DataFetchingEnvironment directly instead")
@Profile("${AppProfiles.session} & ${AppLayer.service}")
class JwtRequestFilter : Filter {
  private val log = LoggerFactory.getLogger(JwtRequestFilter::class.simpleName)

  @Autowired
  private lateinit var authService: AuthService

  override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
    if (request is HttpServletRequest && response is HttpServletResponse) {
      runBlocking {
        runCatching {
          SecurityContextHolder.getContext().authentication =
            toOAuth2AuthenticationToken(authService.interceptToken(request))
        }.onFailure { log.debug(it.message) }
      }
      val attributes = ServletRequestAttributes(request)
      val corrId = StringUtils.trimToNull(request.getHeader(ApiParams.corrId)) ?: newCorrId()
      attributes.setAttribute("corrId", corrId, RequestAttributes.SCOPE_REQUEST)

      val product = StringUtils.trimToNull(request.getHeader(ApiParams.product)) ?: Vertical.feedless.name
      attributes.setAttribute("product", product, RequestAttributes.SCOPE_REQUEST)

      RequestContextHolder.setRequestAttributes(attributes)
    }
    chain.doFilter(request, response)
  }


  private fun toOAuth2AuthenticationToken(jwtToken: Jwt): OAuth2AuthenticationToken {
    val attributes = mapOf("dummy" to "wef")
    var authorities: List<GrantedAuthority> = jwtToken.capabilities()

    val principal: OAuth2User = DefaultOAuth2User(authorities, attributes, "dummy")
    val authorizedClientRegistrationId = jwtToken.getClaimAsString("id")
    return OAuth2AuthenticationToken(
      principal,
      authorities,
      authorizedClientRegistrationId
    )
  }
}

fun Jwt.capabilities(): List<LazyGrantedAuthority> {
  val capabilitiesMaybe = claims[JwtParameterNames.CAPABILITIES]
  if (capabilitiesMaybe is Map<*, *>) {
    try {
      return capabilitiesMaybe.entries.map {
        LazyGrantedAuthority(it.key as String, it.value as String)
      }
    } catch (_: IllegalArgumentException) {
    } catch (_: JsonSyntaxException) {
    }
  }
  return emptyList()
}
