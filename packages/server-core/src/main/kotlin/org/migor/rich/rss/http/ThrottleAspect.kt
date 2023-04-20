package org.migor.rich.rss.http

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.migor.rich.rss.AppProfiles
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Aspect
@Service
@Profile("!${AppProfiles.nothrottle}")
class ThrottleAspect {
  private val log = LoggerFactory.getLogger(ThrottleAspect::class.simpleName)

  @Autowired
  lateinit var cache: RequestThrottleService

  @Around("@annotation(org.migor.rich.rss.http.Throttled)")
  fun aquire(joinPoint: ProceedingJoinPoint): Any? {
    if (cache.tryConsume(joinPoint)) {
      return joinPoint.proceed()
    }
    return null
  }

  //  @Before("execution(* com.gkatzioura.spring.aop.service.SampleService.createSample (java.lang.String)) && args(sampleName)")
//  fun beforeSampleCreation(sampleName: String) {
//    LOGGER.info("A request was issued for a sample name: $sampleName")
//  }
//  @Around("execution(* org.migor.rich.rss.api..*(..)))")
//  @Throws(Throwable::class)
//  open fun profileAllMethods(proceedingJoinPoint: ProceedingJoinPoint): Any? {
//    val methodSignature = proceedingJoinPoint.signature as MethodSignature
//
//    //Get intercepted method details
//    val className = methodSignature.declaringType.simpleName
//    val methodName = methodSignature.name
//    val stopWatch = StopWatch()
//
//    //Measure method execution time
//    stopWatch.start()
//    val result = proceedingJoinPoint.proceed()
//    stopWatch.stop()
//
//    //Log method execution time
//    log.info(
//      "Execution time of " + className + "." + methodName + " "
//        + ":: " + stopWatch.getTotalTimeMillis() + " ms"
//    )
//    return result
//  }

//  @AfterThrowing(pointcut = "controller() && allMethod()", throwing = "exception")
//  fun logAfterThrowing(joinPoint: JoinPoint, exception: Throwable) {
//    log.error("An exception has been thrown in " + joinPoint.signature.name + " ()")
//    log.error("Cause : " + exception.cause)
//  }
}
