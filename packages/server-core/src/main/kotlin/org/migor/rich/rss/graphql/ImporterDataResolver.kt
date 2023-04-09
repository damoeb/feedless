package org.migor.rich.rss.graphql

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import kotlinx.coroutines.coroutineScope
import org.migor.rich.rss.AppProfiles
import org.migor.rich.rss.generated.DgsConstants
import org.migor.rich.rss.generated.types.Bucket
import org.migor.rich.rss.generated.types.Importer
import org.migor.rich.rss.generated.types.NativeFeed
import org.migor.rich.rss.graphql.DtoResolver.toDTO
import org.migor.rich.rss.service.BucketService
import org.migor.rich.rss.service.FeedService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@DgsComponent
@Profile(AppProfiles.database)
class ImporterDataResolver {

  @Autowired
  lateinit var feedService: FeedService

  @Autowired
  lateinit var bucketService: BucketService

  @DgsData(parentType = DgsConstants.IMPORTER.TYPE_NAME)
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun nativeFeed(dfe: DgsDataFetchingEnvironment): NativeFeed = coroutineScope {
    val importer: Importer = dfe.getSource()
    feedService.findNativeById(UUID.fromString(importer.nativeFeedId)).map { toDTO(it) }.orElseThrow()
  }

  @DgsData(parentType = DgsConstants.IMPORTER.TYPE_NAME)
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun bucket(dfe: DgsDataFetchingEnvironment): Bucket = coroutineScope {
    val importer: Importer = dfe.getSource()
    bucketService.findById(UUID.fromString(importer.bucketId)).map { toDTO(it) }.orElseThrow()
  }

}
