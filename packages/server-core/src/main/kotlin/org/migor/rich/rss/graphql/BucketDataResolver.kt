package org.migor.rich.rss.graphql

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import kotlinx.coroutines.coroutineScope
import org.migor.rich.rss.AppProfiles
import org.migor.rich.rss.generated.DgsConstants
import org.migor.rich.rss.graphql.DtoResolver.toDTO
import org.migor.rich.rss.service.ImporterService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*
import org.migor.rich.rss.generated.types.Bucket as BucketDto
import org.migor.rich.rss.generated.types.Importer as ImporterDto

@DgsComponent
@Profile(AppProfiles.database)
class BucketDataResolver {

  @Autowired
  lateinit var importerService: ImporterService

  @DgsData(parentType = DgsConstants.BUCKET.TYPE_NAME)
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun importers(dfe: DgsDataFetchingEnvironment): List<ImporterDto> = coroutineScope {
    val bucket: BucketDto = dfe.getSource()
    importerService.findAllByBucketId(UUID.fromString(bucket.id)).map { toDTO(it) }
  }

//  @DgsData(parentType = DgsConstants.BUCKET.TYPE_NAME)
//  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
//  suspend fun articles(dfe: DgsDataFetchingEnvironment,
//                       @InputArgument filter: ArticlesFilterInputDto) = coroutineScope {
//    val bucket: BucketDto = dfe.getSource()
//    toDTO(articleService.findAllFiltered(UUID.fromString(bucket.streamId), data))
//  }

}
