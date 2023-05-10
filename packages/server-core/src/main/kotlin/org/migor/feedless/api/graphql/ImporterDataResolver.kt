package org.migor.feedless.api.graphql

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import com.netflix.graphql.dgs.InputArgument
import kotlinx.coroutines.coroutineScope
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.graphql.DtoResolver.toDTO
import org.migor.feedless.generated.DgsConstants
import org.migor.feedless.generated.types.Bucket
import org.migor.feedless.generated.types.Histogram
import org.migor.feedless.generated.types.HistogramFrame
import org.migor.feedless.generated.types.Importer
import org.migor.feedless.generated.types.NativeFeed
import org.migor.feedless.service.BucketService
import org.migor.feedless.service.FeedService
import org.migor.feedless.service.HistogramService
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

  @Autowired
  lateinit var histogramService: HistogramService

  @DgsData(parentType = DgsConstants.IMPORTER.TYPE_NAME)
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun nativeFeed(dfe: DgsDataFetchingEnvironment): NativeFeed = coroutineScope {
    val importer: Importer = dfe.getSource()
    feedService.findNativeById(UUID.fromString(importer.nativeFeedId))
      .map { toDTO(it) }
      .orElseThrow { IllegalArgumentException("nativeFeed not found")}
  }

  @DgsData(parentType = DgsConstants.IMPORTER.TYPE_NAME)
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun bucket(dfe: DgsDataFetchingEnvironment): Bucket = coroutineScope {
    val importer: Importer = dfe.getSource()
    bucketService.findById(UUID.fromString(importer.bucketId))
      .map { toDTO(it) }
      .orElseThrow { IllegalArgumentException("bucket not found")}
  }

  @DgsData(parentType = DgsConstants.IMPORTER.TYPE_NAME)
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun histogram(
    dfe: DgsDataFetchingEnvironment,
    @InputArgument("frame") frame: HistogramFrame
  ): Histogram = coroutineScope {
    val importer: Importer = dfe.getSource()
    toDTO(histogramService.histogramByStreamIdOrImporterId(null, UUID.fromString(importer.id), frame), frame)
  }


}
