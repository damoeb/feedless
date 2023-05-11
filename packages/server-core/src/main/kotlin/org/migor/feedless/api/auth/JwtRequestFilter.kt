package org.migor.feedless.api.auth

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.migor.feedless.AppProfiles
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component


@Component
@Profile(AppProfiles.database)
class JwtRequestFilter : Filter {
  private val log = LoggerFactory.getLogger(JwtRequestFilter::class.simpleName)

  @Autowired
  lateinit var authService: AuthService

  override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
    if (request is HttpServletRequest && response is HttpServletResponse) {
      runCatching {
        val token = authService.interceptToken(request)
        SecurityContextHolder.getContext().authentication = token
      }
//      val attributes = ServletRequestAttributes(request)
//      val corrId = if (StringUtils.isBlank(response.getHeader(ApiParams.corrId))) {
//        newCorrId()
//      } else {
//        response.getHeader(ApiParams.corrId)
//      }
//      attributes.setAttribute("corrId", corrId, RequestAttributes.SCOPE_REQUEST)
//      RequestContextHolder.setRequestAttributes(attributes)
    }
    chain.doFilter(request, response)
  }
}
