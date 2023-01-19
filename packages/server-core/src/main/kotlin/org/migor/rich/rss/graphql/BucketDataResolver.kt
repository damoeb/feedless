package org.migor.rich.rss.graphql

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import kotlinx.coroutines.coroutineScope
import org.migor.rich.rss.generated.BucketDto
import org.migor.rich.rss.generated.ImporterDto
import org.migor.rich.rss.graphql.DtoResolver.toDTO
import org.migor.rich.rss.service.ArticleService
import org.migor.rich.rss.service.ImporterService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@DgsComponent
class BucketDataResolver {

  @Autowired
  lateinit var importerService: ImporterService

  @Autowired
  lateinit var articleService: ArticleService

  @DgsData(parentType = "Bucket")
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun importers(dfe: DgsDataFetchingEnvironment): List<ImporterDto> = coroutineScope {
    val bucket: BucketDto = dfe.getSource()
    importerService.findAllByBucketId(UUID.fromString(bucket.id)).map { toDTO(it) }
  }

  @DgsData(parentType = "Bucket")
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun importersCount(dfe: DgsDataFetchingEnvironment): Long = coroutineScope {
    val bucket: BucketDto = dfe.getSource()
    importerService.countByBucketId(UUID.fromString(bucket.id))
  }

  @DgsData(parentType = "Bucket")
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun articlesCount(dfe: DgsDataFetchingEnvironment): Long = coroutineScope {
    val bucket: BucketDto = dfe.getSource()
    runCatching {
      articleService.countByStreamId(UUID.fromString(bucket.streamId))
    }.getOrDefault(0)
  }

//  @DgsData(parentType = "Bucket")
//  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
//  suspend fun articles(dfe: DgsDataFetchingEnvironment,
//                       @InputArgument filter: ArticlesFilterInputDto) = coroutineScope {
//    val bucket: BucketDto = dfe.getSource()
//    toDTO(articleService.findAllFiltered(UUID.fromString(bucket.streamId), data))
//  }

}
