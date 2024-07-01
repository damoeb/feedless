package org.migor.feedless.document

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import com.netflix.graphql.dgs.DgsMutation
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import kotlinx.coroutines.coroutineScope
import org.apache.commons.lang3.StringUtils
import org.migor.feedless.AppProfiles
import org.migor.feedless.NotFoundException
import org.migor.feedless.api.ApiParams
import org.migor.feedless.api.throttle.Throttled
import org.migor.feedless.common.PropertyService
import org.migor.feedless.generated.DgsConstants
import org.migor.feedless.generated.types.Activity
import org.migor.feedless.generated.types.ActivityItem
import org.migor.feedless.generated.types.DeleteWebDocumentsInput
import org.migor.feedless.generated.types.Repository
import org.migor.feedless.generated.types.WebDocument
import org.migor.feedless.generated.types.WebDocumentWhereInput
import org.migor.feedless.generated.types.WebDocumentsInput
import org.migor.feedless.repository.RepositoryService
import org.migor.feedless.session.SessionService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.RequestHeader
import java.util.*

@DgsComponent
@Profile("${AppProfiles.database} & ${AppProfiles.api}")
class DocumentResolver {

  private val log = LoggerFactory.getLogger(DocumentResolver::class.simpleName)

  @Autowired
  private lateinit var repositoryService: RepositoryService

  @Autowired
  private lateinit var sessionService: SessionService

  @Autowired
  private lateinit var propertyService: PropertyService

  @Autowired
  private lateinit var documentService: DocumentService

  @Autowired
  private lateinit var documentDAO: DocumentDAO

  @Throttled
  @DgsQuery
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun webDocument(
    @InputArgument data: WebDocumentWhereInput,
    @RequestHeader(ApiParams.corrId) corrId: String,
  ): WebDocument = coroutineScope {
    log.info("[$corrId] webDocument $data")
    val document = documentService.findById(UUID.fromString(data.where.id)) ?: throw NotFoundException("webDocument not found")
    repositoryService.findById(corrId, document.repositoryId)
    document.toDto(propertyService)
  }

  @Throttled
  @DgsQuery
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun webDocuments(
    @InputArgument data: WebDocumentsInput,
    @RequestHeader(ApiParams.corrId) corrId: String,
  ): List<WebDocument> = coroutineScope {
    log.info("[$corrId] webDocuments $data")
    val repositoryId = UUID.fromString(data.where.repository.where.id)

    val repository = repositoryService.findById(corrId, repositoryId)
    documentService.findAllByRepositoryId(repository.id, data.cursor?.page, data.cursor?.pageSize).get().map { it.toDto(
      propertyService
    )
    }.toList()
  }

  @DgsData(parentType = DgsConstants.REPOSITORY.TYPE_NAME)
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun documentCount(dfe: DgsDataFetchingEnvironment): Long = coroutineScope {
    val repository: Repository = dfe.getSource()
    documentDAO.countByRepositoryId(UUID.fromString(repository.id))
  }

  @DgsMutation
  @PreAuthorize("hasAuthority('USER')")
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun deleteWebDocuments(
    @InputArgument data: DeleteWebDocumentsInput,
    @RequestHeader(ApiParams.corrId) corrId: String,
  ): Boolean = coroutineScope {
    documentService.deleteDocuments(corrId, sessionService.user(corrId), UUID.fromString(data.where.repository.where.id), data.where.id)
    true
  }

  @DgsData(parentType = DgsConstants.REPOSITORY.TYPE_NAME)
  suspend fun activity(
    dfe: DgsDataFetchingEnvironment,
  ): Activity = coroutineScope {
    val repository: Repository = dfe.getSource()

    Activity.newBuilder()
      .items(documentService.getDocumentFrequency(UUID.fromString(repository.id)).map { it.toDto() })
      .build()
  }

}

private fun FrequencyItem.toDto(): ActivityItem {
  fun leftPad(num: Int): String {
    return StringUtils.leftPad("$num", 2, "0")
  }

  return ActivityItem.newBuilder()
    .index("${year}${leftPad(month)}${leftPad(day)}")
    .count(count)
    .build()
}