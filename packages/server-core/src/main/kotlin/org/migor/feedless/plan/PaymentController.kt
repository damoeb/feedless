package org.migor.feedless.plan

import kotlinx.coroutines.coroutineScope
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.common.PropertyService
import org.migor.feedless.user.corrId
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
@Profile("${AppProfiles.plan} & ${AppLayer.api}")
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
  ): ResponseEntity<String> = coroutineScope {
    val corrId = kotlin.coroutines.coroutineContext.corrId()
    log.info("[$corrId] paymentCallback $billingId")
    val headers = HttpHeaders()
    val queryParams = try {
      orderService.handlePaymentCallback(billingId)
      "success=true"
    } catch (ex: Exception) {
      log.error("Payment callback failed with ${ex.message}", ex)
      "success=false&message=${ex.message}"
    }
    headers.add(HttpHeaders.LOCATION, "${propertyService.appHost}/payment/summary/${billingId}?$queryParams")
    ResponseEntity<String>(headers, HttpStatus.FOUND)
  }

}
