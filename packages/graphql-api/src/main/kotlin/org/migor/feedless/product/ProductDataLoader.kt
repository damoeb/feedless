package org.migor.feedless.product

import com.netflix.graphql.dgs.DgsDataLoader
import org.dataloader.MappedBatchLoader
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.mapper.toDto
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import org.migor.feedless.generated.types.Product as ProductDto


@Profile("${AppProfiles.plan} & ${AppLayer.api}")
@DgsDataLoader(name = "product")
class ProductDataLoader : MappedBatchLoader<ProductId, ProductDto> {

    @Autowired
    lateinit var productRepository: ProductRepository

    override fun load(ids: MutableSet<ProductId>): CompletionStage<MutableMap<ProductId, ProductDto>> {
        return CompletableFuture.supplyAsync {
            productRepository.findAllByIdIn(ids.distinct())
                .map { it.toDto() }
                .fold(mutableMapOf()) { acc, item ->
                    acc[ProductId(item.id)] = item
                    acc
                }
        }
    }

}
