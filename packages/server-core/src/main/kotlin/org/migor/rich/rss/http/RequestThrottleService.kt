package org.migor.rich.rss.http

import org.aspectj.lang.ProceedingJoinPoint

abstract class RequestThrottleService {
  abstract fun tryConsume(joinPoint: ProceedingJoinPoint): Boolean
}
