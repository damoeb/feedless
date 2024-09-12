package org.migor.feedless.document

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import com.netflix.graphql.dgs.DgsMutation
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import graphql.schema.DataFetchingEnvironment
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.NotFoundException
import org.migor.feedless.api.ApiParams
import org.migor.feedless.api.throttle.Throttled
import org.migor.feedless.common.PropertyService
import org.migor.feedless.generated.DgsConstants
import org.migor.feedless.generated.types.DatesWhereInput
import org.migor.feedless.generated.types.DeleteWebDocumentsInput
import org.migor.feedless.generated.types.DocumentFrequency
import org.migor.feedless.generated.types.Repository
import org.migor.feedless.generated.types.RepositoryUniqueWhereInput
import org.migor.feedless.generated.types.WebDocument
import org.migor.feedless.generated.types.WebDocumentDateField
import org.migor.feedless.generated.types.WebDocumentWhereInput
import org.migor.feedless.generated.types.WebDocumentsInput
import org.migor.feedless.generated.types.WebDocumentsWhereInput
import org.migor.feedless.repository.RepositoryService
import org.migor.feedless.repository.toPageRequest
import org.migor.feedless.session.SessionService
import org.migor.feedless.session.useRequestContext
import org.migor.feedless.util.toMillis
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.RequestHeader
import java.time.LocalDateTime
import java.util.*

@DgsComponent
@Profile("${AppProfiles.document} & ${AppLayer.api}")
@Transactional
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

  @Throttled
  @DgsQuery
  suspend fun webDocument(
    dfe: DataFetchingEnvironment,
    @InputArgument data: WebDocumentWhereInput,
    @RequestHeader(ApiParams.corrId) corrId: String,
  ): WebDocument = withContext(useRequestContext(currentCoroutineContext(), dfe)) {
    log.debug("[$corrId] webDocument $data")
    val document =
      documentService.findById(UUID.fromString(data.where.id)) ?: throw NotFoundException("webDocument not found")
    repositoryService.findById(corrId, document.repositoryId)
    document.toDto(propertyService)
  }

  @Throttled
  @DgsQuery(field = DgsConstants.QUERY.WebDocuments)
  suspend fun webDocuments(
    dfe: DataFetchingEnvironment,
    @InputArgument data: WebDocumentsInput,
    @RequestHeader(ApiParams.corrId) corrId: String,
  ): List<WebDocument> = withContext(useRequestContext(currentCoroutineContext(), dfe)) {
    log.debug("[$corrId] webDocuments $data")
    val repositoryId = UUID.fromString(data.where.repository.id)

    val repository = repositoryService.findById(corrId, repositoryId)
    val pageable = toPageRequest(data.cursor.page, data.cursor.pageSize ?: 10)
    documentService.findAllByRepositoryId(repository.id, data.where, data.orderBy, pageable = pageable).mapNotNull {
      it?.toDto(
        propertyService
      )
    }.toList()
  }

  @DgsData(parentType = DgsConstants.REPOSITORY.TYPE_NAME, field = DgsConstants.REPOSITORY.DocumentCount)
  suspend fun documentCount(dfe: DgsDataFetchingEnvironment): Long = coroutineScope {
    val repository: Repository = dfe.getSource()!!
    documentService.countByRepositoryId(UUID.fromString(repository.id))
  }

  @DgsMutation(field = DgsConstants.MUTATION.DeleteWebDocuments)
  @PreAuthorize("hasAuthority('USER')")
  suspend fun deleteWebDocuments(
    dfe: DataFetchingEnvironment,
    @InputArgument data: DeleteWebDocumentsInput,
    @RequestHeader(ApiParams.corrId) corrId: String,
  ): Boolean = withContext(useRequestContext(currentCoroutineContext(), dfe)) {
    documentService.deleteDocuments(
      corrId,
      sessionService.user(corrId),
      UUID.fromString(data.where.repository.id),
      data.where.id!!
    )
    true
  }

  @DgsQuery(field = DgsConstants.QUERY.WebDocumentsFrequency)
  suspend fun webDocumentsFrequency(
    @InputArgument where: WebDocumentsWhereInput,
    @InputArgument groupBy: WebDocumentDateField,
  ): List<DocumentFrequency> = coroutineScope {
    documentService.getDocumentFrequency(where, groupBy)
  }


  @DgsData(parentType = DgsConstants.REPOSITORY.TYPE_NAME, field = DgsConstants.REPOSITORY.Frequency)
  suspend fun frequency(
    dfe: DgsDataFetchingEnvironment,
  ): List<DocumentFrequency> = coroutineScope {
    val repository: Repository = dfe.getSource()!!
    documentService.getDocumentFrequency(
      WebDocumentsWhereInput(
        repository = RepositoryUniqueWhereInput(id = repository.id),
        createdAt = DatesWhereInput(after = LocalDateTime.now().minusMonths(1).toMillis())
      ),
      WebDocumentDateField.createdAt
    )
  }

}
