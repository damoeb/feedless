package org.migor.feedless.plan

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import org.migor.feedless.AppProfiles
import org.migor.feedless.common.PropertyService
import org.migor.feedless.session.useRequestContext
import org.migor.feedless.util.CryptUtil.newCorrId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@Controller
@Profile("${AppProfiles.saas} & ${AppProfiles.api}")
class PaymentController {

  private val log = LoggerFactory.getLogger(PaymentController::class.simpleName)

  @Autowired
  lateinit var orderService: OrderService

  @Autowired
  lateinit var propertyService: PropertyService

  @GetMapping(
    "/payment/{billingId}/callback",
  )
  suspend fun paymentCallback(
    @PathVariable("billingId") billingId: String,
  ): ResponseEntity<String> = withContext(useRequestContext(currentCoroutineContext())) {
    val corrId = newCorrId()
    log.info("[$corrId] paymentCallback $billingId")
    val headers = HttpHeaders()
    val queryParams = try {
      orderService.handlePaymentCallback(corrId, billingId)
      "success=true"
    } catch (ex: Exception) {
      log.error("Payment callback failed with ${ex.message}", ex)
      "success=false&message=${ex.message}"
    }
    headers.add(HttpHeaders.LOCATION, "${propertyService.appHost}/payment/summary/${billingId}?$queryParams")
    ResponseEntity<String>(headers, HttpStatus.FOUND)
  }

}
