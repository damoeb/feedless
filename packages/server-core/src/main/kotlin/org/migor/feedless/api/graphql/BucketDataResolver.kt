package org.migor.feedless.api.graphql

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import com.netflix.graphql.dgs.InputArgument
import kotlinx.coroutines.coroutineScope
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.graphql.DtoResolver.toDTO
import org.migor.feedless.data.jpa.repositories.ImporterDAO
import org.migor.feedless.generated.DgsConstants
import org.migor.feedless.generated.types.Histogram
import org.migor.feedless.generated.types.HistogramFrame
import org.migor.feedless.service.HistogramService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*
import org.migor.feedless.generated.types.Bucket as BucketDto

@DgsComponent
@Profile(AppProfiles.database)
class BucketDataResolver {

  @Autowired
  lateinit var importerDAO: ImporterDAO

  @Autowired
  lateinit var histogramService: HistogramService

  @DgsData(parentType = DgsConstants.BUCKET.TYPE_NAME)
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun importersCount(dfe: DgsDataFetchingEnvironment): Int = coroutineScope {
    val bucket: BucketDto = dfe.getSource()
    importerDAO.countByBucketId(UUID.fromString(bucket.id))
  }

  @DgsData(parentType = DgsConstants.BUCKET.TYPE_NAME)
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun histogram(
    dfe: DgsDataFetchingEnvironment,
    @InputArgument("frame") frame: HistogramFrame
  ): Histogram = coroutineScope {
    val bucket: BucketDto = dfe.getSource()

    toDTO(histogramService.histogramByStreamIdOrImporterId(UUID.fromString(bucket.streamId), null, frame), frame)
  }
}
