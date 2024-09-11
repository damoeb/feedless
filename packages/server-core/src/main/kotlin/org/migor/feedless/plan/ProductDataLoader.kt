package org.migor.feedless.plan

import com.netflix.graphql.dgs.DgsDataLoader
import org.dataloader.MappedBatchLoader
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.generated.types.Product
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage


@Profile("${AppProfiles.plan} & ${AppLayer.api}")
@DgsDataLoader(name = "product")
class ProductDataLoader : MappedBatchLoader<String, Product> {

  @Autowired
  lateinit var productDAO: ProductDAO

  override fun load(ids: MutableSet<String>): CompletionStage<MutableMap<String, Product>> {
    return CompletableFuture.supplyAsync {
      productDAO.findAllById(ids.distinct().map { UUID.fromString(it) })
        .fold(mutableMapOf()) { acc, item ->
          acc[item.id.toString()] = item.toDTO()
          acc
        }
    }
  }

}
