package org.migor.feedless.plan

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import kotlinx.coroutines.coroutineScope
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.ApiParams
import org.migor.feedless.api.throttle.Throttled
import org.migor.feedless.generated.DgsConstants
import org.migor.feedless.generated.types.CloudSubscription
import org.migor.feedless.generated.types.PricedProduct
import org.migor.feedless.generated.types.Product
import org.migor.feedless.generated.types.ProductsWhereInput
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.RequestHeader
import java.util.*

@DgsComponent
@Profile("${AppProfiles.database} & ${AppProfiles.api}")
class ProductResolver {

  @Autowired
  private lateinit var productDAO: ProductDAO
  private val log = LoggerFactory.getLogger(ProductResolver::class.simpleName)

  @Autowired
  lateinit var productService: ProductService

  @Autowired
  lateinit var pricedProductDAO: PricedProductDAO

  @Autowired
  lateinit var featureGroupDAO: FeatureGroupDAO

  @Throttled
  @DgsQuery
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun products(
    @RequestHeader(ApiParams.corrId) corrId: String,
    @InputArgument data: ProductsWhereInput
  ): List<Product> =
    coroutineScope {
      log.info("[$corrId] products $data")
      productService.findAll(data).map { it.toDTO() }
    }

  @DgsData(parentType = DgsConstants.CLOUDSUBSCRIPTION.TYPE_NAME)
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun product(dfe: DgsDataFetchingEnvironment): Product = coroutineScope {
    val cloudsubscription: CloudSubscription = dfe.getSource()
    productDAO.findById(UUID.fromString(cloudsubscription.productId)).orElseThrow().toDTO()
  }

  @DgsData(parentType = DgsConstants.PRODUCT.TYPE_NAME)
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun prices(dfe: DgsDataFetchingEnvironment): List<PricedProduct> = coroutineScope {
    val product: Product = dfe.getSource()
    pricedProductDAO.findAllByProductId(UUID.fromString(product.id)).map { it.toDto() }
  }
}
