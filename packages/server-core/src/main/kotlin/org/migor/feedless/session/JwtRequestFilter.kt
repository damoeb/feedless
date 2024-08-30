package org.migor.feedless.session

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kotlinx.coroutines.runBlocking
import org.apache.commons.lang3.StringUtils
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.ApiParams
import org.migor.feedless.data.jpa.enums.ProductCategory
import org.migor.feedless.util.CryptUtil.newCorrId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes


@Component
@Profile(AppProfiles.database)
class JwtRequestFilter : Filter {
  private val log = LoggerFactory.getLogger(JwtRequestFilter::class.simpleName)

  @Autowired
  private lateinit var authService: AuthService

  override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
    if (request is HttpServletRequest && response is HttpServletResponse) {
      runBlocking {
        runCatching {
          val token = authService.interceptToken(request)

          SecurityContextHolder.getContext().authentication = token
        }
      }
      val attributes = ServletRequestAttributes(request)
      val corrId = StringUtils.trimToNull(request.getHeader(ApiParams.corrId)) ?: newCorrId()
      attributes.setAttribute("corrId", corrId, RequestAttributes.SCOPE_REQUEST)

      val product = StringUtils.trimToNull(request.getHeader(ApiParams.product)) ?: ProductCategory.feedless.name
      attributes.setAttribute("product", product, RequestAttributes.SCOPE_REQUEST)

      RequestContextHolder.setRequestAttributes(attributes)
    }
    chain.doFilter(request, response)
  }
}
