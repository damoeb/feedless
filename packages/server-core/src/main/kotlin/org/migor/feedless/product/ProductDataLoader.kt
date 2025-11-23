package org.migor.feedless.product

import com.netflix.graphql.dgs.DgsDataLoader
import org.dataloader.MappedBatchLoader
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.mapper.toDto
import org.migor.feedless.data.jpa.product.ProductDAO
import org.migor.feedless.data.jpa.product.toDomain
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import org.migor.feedless.generated.types.Product as ProductDto


@Profile("${AppProfiles.plan} & ${AppLayer.api}")
@DgsDataLoader(name = "product")
class ProductDataLoader : MappedBatchLoader<ProductId, ProductDto> {

    @Autowired
    lateinit var productDAO: ProductDAO

    override fun load(ids: MutableSet<ProductId>): CompletionStage<MutableMap<ProductId, ProductDto>> {
        return CompletableFuture.supplyAsync {
            productDAO.findAllById(ids.distinct().map { it.uuid })
                .map { it.toDomain().toDto() }
                .fold(mutableMapOf()) { acc, item ->
                    acc[ProductId(item.id)] = item
                    acc
                }
        }
    }

}
