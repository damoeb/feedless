package org.migor.rich.rss.http

import org.aspectj.lang.ProceedingJoinPoint
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

abstract class RequestThrottleService {
  abstract fun tryConsume(joinPoint: ProceedingJoinPoint): Boolean
  fun deny(joinPoint: ProceedingJoinPoint) {
    val response = (RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes).response
    response?.let {
      response.sendError(429)
    }
  }
}
