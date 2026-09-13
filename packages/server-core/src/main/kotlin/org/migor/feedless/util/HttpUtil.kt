package org.migor.feedless.util

import jakarta.servlet.http.HttpServletRequest
import org.apache.commons.lang3.StringUtils
import org.migor.feedless.api.ApiParams
import org.migor.feedless.capability.CORR_ID_REQUEST_ATTR

object HttpUtil {
  fun getRemoteAddr(request: HttpServletRequest): String {
    return StringUtils.trimToNull(request.getHeader("X-Real-IP")) ?: request.remoteAddr
  }

  /** The request's correlation id — the client's x-corr-id or a new one — kept once chosen so every filter shares it. */
  fun corrIdOf(request: HttpServletRequest): String =
    request.getAttribute(CORR_ID_REQUEST_ATTR) as? String
      ?: (StringUtils.trimToNull(request.getHeader(ApiParams.corrId)) ?: CryptUtil.newCorrId())
        .also { request.setAttribute(CORR_ID_REQUEST_ATTR, it) }
}
