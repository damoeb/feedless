package org.migor.rich.rss.util

import jakarta.servlet.http.HttpServletRequest
import org.apache.commons.lang3.StringUtils
import java.util.*

object HttpUtil {
  fun getRemoteAddr(request: HttpServletRequest): String {
    return Optional.ofNullable(StringUtils.trimToNull(request.getHeader("X-Real-IP"))).orElse(request.remoteAddr)
  }
}
