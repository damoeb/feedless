package org.migor.feedless.api.throttle

import org.aspectj.lang.ProceedingJoinPoint

abstract class RequestThrottleService {
  abstract suspend fun tryConsume(joinPoint: ProceedingJoinPoint): Boolean
}
