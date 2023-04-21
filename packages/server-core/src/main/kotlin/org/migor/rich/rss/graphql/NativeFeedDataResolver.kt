package org.migor.rich.rss.graphql

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import com.netflix.graphql.dgs.InputArgument
import kotlinx.coroutines.coroutineScope
import org.migor.rich.rss.AppProfiles
import org.migor.rich.rss.generated.DgsConstants
import org.migor.rich.rss.generated.types.Bucket
import org.migor.rich.rss.generated.types.GenericFeed
import org.migor.rich.rss.generated.types.Histogram
import org.migor.rich.rss.generated.types.HistogramFrame
import org.migor.rich.rss.generated.types.Importer
import org.migor.rich.rss.generated.types.NativeFeed
import org.migor.rich.rss.graphql.DtoResolver.toDTO
import org.migor.rich.rss.service.GenericFeedService
import org.migor.rich.rss.service.HistogramService
import org.migor.rich.rss.service.ImporterService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@DgsComponent
@Profile(AppProfiles.database)
class NativeFeedDataResolver {

  @Autowired
  lateinit var genericFeedService: GenericFeedService

  @Autowired
  lateinit var importerService: ImporterService

  @Autowired
  lateinit var histogramService: HistogramService

  @DgsData(parentType = DgsConstants.NATIVEFEED.TYPE_NAME)
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun genericFeed(dfe: DgsDataFetchingEnvironment): GenericFeed? = coroutineScope {
    val feed: NativeFeed = dfe.getSource()
    genericFeedService.findByNativeFeedId(UUID.fromString(feed.id)).map { toDTO(it) }.orElse(null)
  }

  @DgsData(parentType = DgsConstants.NATIVEFEED.TYPE_NAME)
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun importers(dfe: DgsDataFetchingEnvironment): List<Importer> = coroutineScope {
    val feed: NativeFeed = dfe.getSource()
    importerService.findAllByFeedId(UUID.fromString(feed.id)).map { toDTO(it) }
  }

  @DgsData(parentType = DgsConstants.NATIVEFEED.TYPE_NAME)
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun histogram(
    dfe: DgsDataFetchingEnvironment,
    @InputArgument("frame") frame: HistogramFrame
  ): Histogram = coroutineScope {
    val bucket: Bucket = dfe.getSource()

    toDTO(histogramService.histogramByStreamIdOrImporterId(UUID.fromString(bucket.streamId), null, frame), frame)
  }


}
