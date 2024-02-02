package org.migor.feedless.service

import org.migor.feedless.data.jpa.enums.ProductName
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ProductService {

  private val log = LoggerFactory.getLogger(ProductService::class.simpleName)

  @Autowired
  lateinit var propertyService: PropertyService

  fun getDomain(product: ProductName): String {
    return when(product) {
      ProductName.visualDiff -> "visualdiff.com"
      ProductName.pageChangeTracker -> "pagechangetracker.com"
      ProductName.rssBuilder -> "feedguru.com"
      ProductName.feedless -> "feedless.com"
      ProductName.custom -> propertyService.domain
      else -> throw IllegalArgumentException("not supported")
    }
  }

  fun getAppUrl(product: ProductName): String {
    return if (product == ProductName.custom) {
      propertyService.appHost
    } else {
      "https://${getDomain(product)}"
    }
  }

  fun getGatewayUrl(product: ProductName): String {
    return if (product == ProductName.custom) {
      propertyService.apiGatewayUrl
    } else {
      "https://api.${getDomain(product)}"
    }
  }


}
