package org.migor.rich.rss.util

import org.apache.commons.lang3.StringUtils
import java.util.*
import javax.servlet.http.HttpServletRequest

object HttpUtil {
  fun getRemoteAddr(request: HttpServletRequest): String {
    return Optional.ofNullable(StringUtils.trimToNull(request.getHeader("X-Real-IP"))).orElse(request.remoteAddr)
  }
}
