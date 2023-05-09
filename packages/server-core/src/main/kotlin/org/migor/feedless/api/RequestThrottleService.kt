package org.migor.feedless.api

import org.aspectj.lang.ProceedingJoinPoint

abstract class RequestThrottleService {
  abstract fun tryConsume(joinPoint: ProceedingJoinPoint): Boolean
}
