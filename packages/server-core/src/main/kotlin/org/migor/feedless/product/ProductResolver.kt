package org.migor.feedless.product

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import kotlinx.coroutines.coroutineScope
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.mapper.toDto
import org.migor.feedless.api.throttle.Throttled
import org.migor.feedless.feature.FeatureService
import org.migor.feedless.generated.DgsConstants
import org.migor.feedless.generated.types.FeatureGroup
import org.migor.feedless.generated.types.PricedProduct
import org.migor.feedless.generated.types.Product
import org.migor.feedless.generated.types.ProductsWhereInput
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile

@DgsComponent
@Profile("${AppProfiles.plan} & ${AppLayer.repository}")
class ProductResolver(
  val productService: ProductUseCaseImpl,
  val featureService: FeatureService
) {

  private val log = LoggerFactory.getLogger(ProductResolver::class.simpleName)

  @Throttled
  @DgsQuery(field = DgsConstants.QUERY.Products)
  suspend fun getProducts(
    @InputArgument(DgsConstants.QUERY.PRODUCTS_INPUT_ARGUMENT.Data) data: ProductsWhereInput
  ): List<Product> {
    log.debug("products $data")
    return productService.findAll(data).map { it.toDto() }
  }

//  @DgsData(parentType = DgsConstants.CLOUDSUBSCRIPTION.TYPE_NAME)
//  suspend fun product(dfe: DgsDataFetchingEnvironment): Product = coroutineScope {
//    val cloudsubscription: Plan = dfe.getSourceOrThrow()
//    productDAO.findById(UUID.fromString(cloudsubscription.productId)).orElseThrow().toDTO()
//  }

  @DgsData(parentType = DgsConstants.PRODUCT.TYPE_NAME, field = DgsConstants.PRODUCT.Prices)
  suspend fun getPrices(dfe: DgsDataFetchingEnvironment): List<PricedProduct> = coroutineScope {
    val product: Product = dfe.getSourceOrThrow()
    productService.findAllByProductId(ProductId(product.id)).map { it.toDto() }
  }

  @DgsData(parentType = DgsConstants.PRODUCT.TYPE_NAME, field = DgsConstants.PRODUCT.FeatureGroup)
  suspend fun getFeatureGroup(dfe: DgsDataFetchingEnvironment): FeatureGroup? = coroutineScope {
    val product: Product = dfe.getSourceOrThrow()
    // todo implement
//        product.featureGroupId?.let { featureGroupId ->
//            FeatureGroup(
//                id = featureGroupId,
//                name = "",
//                features = featureService.findAllByGroupId(FeatureGroupId(featureGroupId), true)
//            )
//        }
    null
  }
}
