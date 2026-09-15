package org.migor.feedless.http

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Stamps every /api/v1 response with the instance version so feedctl can warn on a mismatch.
 * Runs before HttpApiJwtFilter, so even its 401 carries the header.
 */
@Component
@Profile("${AppProfiles.session} & ${AppLayer.service}")
class HttpApiVersionHeaderFilter(
  @Value("\${app.version}") private val appVersion: String,
) : OncePerRequestFilter() {

  companion object {
    const val VERSION_HEADER = "X-Feedless-Version"
  }

  override fun shouldNotFilter(request: HttpServletRequest): Boolean {
    return !request.requestURI.startsWith("/api/v1/")
  }

  override fun doFilterInternal(
    request: HttpServletRequest,
    response: HttpServletResponse,
    filterChain: FilterChain,
  ) {
    response.setHeader(VERSION_HEADER, appVersion)
    filterChain.doFilter(request, response)
  }
}
