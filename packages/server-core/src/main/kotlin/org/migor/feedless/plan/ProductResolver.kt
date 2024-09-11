package org.migor.feedless.plan

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.ApiParams
import org.migor.feedless.api.throttle.Throttled
import org.migor.feedless.generated.DgsConstants
import org.migor.feedless.generated.types.PricedProduct
import org.migor.feedless.generated.types.Product
import org.migor.feedless.generated.types.ProductsWhereInput
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.RequestHeader
import java.util.*

@DgsComponent
@Profile("${AppProfiles.plan} & ${AppLayer.repository}")
@Transactional
class ProductResolver {

  private val log = LoggerFactory.getLogger(ProductResolver::class.simpleName)

  @Autowired
  lateinit var productService: ProductService

  @Autowired
  lateinit var pricedProductDAO: PricedProductDAO

  @Throttled
  @DgsQuery
  suspend fun products(
    @RequestHeader(ApiParams.corrId) corrId: String,
    @InputArgument data: ProductsWhereInput
  ): List<Product> {
    log.debug("[$corrId] products $data")
    return productService.findAll(data).map { it.toDTO() }
  }

//  @DgsData(parentType = DgsConstants.CLOUDSUBSCRIPTION.TYPE_NAME)
//  @Transactional
//  suspend fun product(dfe: DgsDataFetchingEnvironment): Product = coroutineScope {
//    val cloudsubscription: Plan = dfe.getSource()!!
//    productDAO.findById(UUID.fromString(cloudsubscription.productId)).orElseThrow().toDTO()
//  }

  @DgsData(parentType = DgsConstants.PRODUCT.TYPE_NAME, field = DgsConstants.PRODUCT.Prices)
  suspend fun prices(dfe: DgsDataFetchingEnvironment): List<PricedProduct> = coroutineScope {
    val product: Product = dfe.getSource()!!
    withContext(Dispatchers.IO) {
      pricedProductDAO.findAllByProductId(UUID.fromString(product.id)).map { it.toDto() }
    }
  }
}
