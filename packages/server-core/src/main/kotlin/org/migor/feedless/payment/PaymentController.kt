package org.migor.feedless.payment

import kotlinx.coroutines.coroutineScope
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.common.PropertyService
import org.migor.feedless.order.OrderId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@Controller
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.repository} & ${AppLayer.api}")
class PaymentController {

  private val log = LoggerFactory.getLogger(PaymentController::class.simpleName)

  @Autowired
  private lateinit var paymentService: PaymentService

  @Autowired
  private lateinit var propertyService: PropertyService


  @GetMapping(
    "/payment/{orderId}/callback",
  )
  suspend fun paymentCallback(
    @PathVariable("orderId") orderId: String,
  ): ResponseEntity<String> = coroutineScope {
    log.info("paymentCallback $orderId")
    val headers = HttpHeaders()
    val queryParams = try {
      paymentService.handlePaymentCallback(OrderId(orderId))
      "success=true"
    } catch (ex: Exception) {
      log.error("Payment callback failed with ${ex.message}", ex)
      "success=false&message=${ex.message}"
    }
    headers.add(HttpHeaders.LOCATION, "${propertyService.appHost}/payment/summary/${orderId}?$queryParams")
    ResponseEntity<String>(headers, HttpStatus.FOUND)
  }

}
