package org.migor.feedless.api.throttle

import org.aspectj.lang.ProceedingJoinPoint

abstract class RequestThrottleService {
  abstract fun tryConsume(joinPoint: ProceedingJoinPoint): Boolean
}
