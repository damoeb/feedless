package org.migor.rich.rss.http

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.util.StopWatch
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes


@Aspect
@Component
class RequestThrottleService {
  private val log = LoggerFactory.getLogger(RequestThrottleService::class.simpleName)

//  @Around("execution(* com.gkatzioura.spring.aop.service.SampleService.createSample (java.lang.String)) && args(sampleName)")
//  @Throws(
//    Throwable::class
//  )
//  fun aroundSampleCreation(proceedingJoinPoint: ProceedingJoinPoint, sampleName: String): Any? {
//    var sampleName = sampleName
//    LOGGER.info("A request was issued for a sample name: $sampleName")
//    sampleName = "$sampleName!"
//    val sample: Sample = proceedingJoinPoint.proceed(arrayOf<Any>(sampleName)) as Sample
//    sample.setName(sample.getName().toUpperCase())
//    return sample
//  }

  @Around("@annotation(org.migor.rich.rss.http.Throttled)")
  @Throws(Throwable::class)
  fun trace(joinPoint: ProceedingJoinPoint): Any? {
    val request = (RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes).request
    val methodSignature = joinPoint.signature as MethodSignature
    val className = methodSignature.declaringType.simpleName
    val methodName = methodSignature.name
    val stopWatch = StopWatch()
    stopWatch.start()
    val result = joinPoint.proceed()
    stopWatch.stop()
//
//    //Log method execution time
    log.info(
      "Execution time of " + className + "." + methodName + " "
        + ":: " + stopWatch.getTotalTimeMillis() + " ms"
    )
  return result
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
