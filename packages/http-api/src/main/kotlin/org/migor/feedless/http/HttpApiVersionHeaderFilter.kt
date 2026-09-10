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
 * Stamps every `/api/v1` response with the running instance's version, so `feedctl` can
 * compare its own build version against the instance it talks to and warn on mismatch.
 *
 * Registered in `SecurityConfig` with the same profile as `HttpApiJwtFilter` and ordered
 * immediately before it in the security filter chain, so the header is already on the
 * response by the time that filter (or any downstream handler/exception handler) writes a
 * status — including the 401 `HttpApiJwtFilter` sends for an unauthenticated request, before
 * a controller ever runs.
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
