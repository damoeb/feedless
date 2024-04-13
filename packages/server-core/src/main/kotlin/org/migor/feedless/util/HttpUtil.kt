package org.migor.feedless.util

import jakarta.servlet.http.HttpServletRequest
import org.apache.commons.lang3.StringUtils
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

object HttpUtil {
  fun getRemoteAddr(request: HttpServletRequest): String {
    return StringUtils.trimToNull(request.getHeader("X-Real-IP")) ?: request.remoteAddr
  }

  fun createCorrId(request: HttpServletRequest): String {
    val corrId = CryptUtil.newCorrId()
    val attributes = ServletRequestAttributes(request)
    attributes.setAttribute("corrId", corrId, RequestAttributes.SCOPE_REQUEST)
    RequestContextHolder.setRequestAttributes(attributes)
    return corrId
  }
}
