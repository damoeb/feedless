package org.migor.rich.rss.graphql

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import kotlinx.coroutines.coroutineScope
import org.migor.rich.rss.generated.BucketDto
import org.migor.rich.rss.generated.ImporterDto
import org.migor.rich.rss.generated.NativeFeedDto
import org.migor.rich.rss.graphql.DtoResolver.toDTO
import org.migor.rich.rss.service.BucketService
import org.migor.rich.rss.service.FeedService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@DgsComponent
class ImporterDataResolver {

  @Autowired
  lateinit var feedService: FeedService

  @Autowired
  lateinit var bucketService: BucketService

  @DgsData(parentType = "Importer")
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun nativeFeed(dfe: DgsDataFetchingEnvironment): NativeFeedDto = coroutineScope {
    val importer: ImporterDto = dfe.getSource()
    feedService.findNativeById(UUID.fromString(importer.nativeFeedId)).map { toDTO(it) }.orElseThrow()
  }

  @DgsData(parentType = "Importer")
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun bucket(dfe: DgsDataFetchingEnvironment): BucketDto = coroutineScope {
    val importer: ImporterDto = dfe.getSource()
    bucketService.findById(UUID.fromString(importer.bucketId)).map { toDTO(it) }.orElseThrow()
  }

}
