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
import org.migor.feedless.api.throttle.Throttled
import org.migor.feedless.common.PropertyService
import org.migor.feedless.generated.DgsConstants
import org.migor.feedless.generated.types.CreateRecordInput
import org.migor.feedless.generated.types.DatesWhereInput
import org.migor.feedless.generated.types.DeleteRecordsInput
import org.migor.feedless.generated.types.Record
import org.migor.feedless.generated.types.RecordDateField
import org.migor.feedless.generated.types.RecordFrequency
import org.migor.feedless.generated.types.RecordWhereInput
import org.migor.feedless.generated.types.RecordsInput
import org.migor.feedless.generated.types.RecordsWhereInput
import org.migor.feedless.generated.types.Repository
import org.migor.feedless.generated.types.RepositoryUniqueWhereInput
import org.migor.feedless.repository.RepositoryService
import org.migor.feedless.repository.toPageRequest
import org.migor.feedless.session.SessionService
import org.migor.feedless.session.injectCurrentUser
import org.migor.feedless.util.toMillis
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.transaction.annotation.Transactional
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
  suspend fun record(
    dfe: DataFetchingEnvironment,
    @InputArgument data: RecordWhereInput,
  ): Record = withContext(injectCurrentUser(currentCoroutineContext(), dfe)) {
    log.debug("record $data")
    val document =
      documentService.findById(UUID.fromString(data.where.id)) ?: throw NotFoundException("record not found")
    repositoryService.findById(document.repositoryId)
    document.toDto(propertyService)
  }

  @Throttled
  @DgsQuery(field = DgsConstants.QUERY.Records)
  suspend fun records(
    dfe: DataFetchingEnvironment,
    @InputArgument data: RecordsInput,
  ): List<Record> = withContext(injectCurrentUser(currentCoroutineContext(), dfe)) {
    log.debug("records $data")
    val repositoryId = UUID.fromString(data.where.repository.id)

    val repository = repositoryService.findById(repositoryId)
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

  @DgsMutation(field = DgsConstants.MUTATION.DeleteRecords)
  @PreAuthorize("hasAuthority('USER')")
  suspend fun deleteRecords(
    dfe: DataFetchingEnvironment,
    @InputArgument data: DeleteRecordsInput,
  ): Boolean = withContext(injectCurrentUser(currentCoroutineContext(), dfe)) {
    documentService.deleteDocuments(
      sessionService.user(),
      UUID.fromString(data.where.repository.id),
      data.where.id!!
    )
    true
  }

  @DgsMutation(field = DgsConstants.MUTATION.CreateRecords)
  @PreAuthorize("hasAuthority('USER')")
  suspend fun createRecords(
    dfe: DataFetchingEnvironment,
    @InputArgument records: List<CreateRecordInput>?,
  ): List<Record> = withContext(injectCurrentUser(currentCoroutineContext(), dfe)) {
    emptyList()
  }

  @DgsQuery(field = DgsConstants.QUERY.RecordsFrequency)
  suspend fun recordsFrequency(
    @InputArgument where: RecordsWhereInput,
    @InputArgument groupBy: RecordDateField,
  ): List<RecordFrequency> = coroutineScope {
    documentService.getRecordFrequency(where, groupBy)
  }


  @DgsData(parentType = DgsConstants.REPOSITORY.TYPE_NAME, field = DgsConstants.REPOSITORY.Frequency)
  suspend fun frequency(
    dfe: DgsDataFetchingEnvironment,
  ): List<RecordFrequency> = coroutineScope {
    val repository: Repository = dfe.getSource()!!
    documentService.getRecordFrequency(
      RecordsWhereInput(
        repository = RepositoryUniqueWhereInput(id = repository.id),
        createdAt = DatesWhereInput(after = LocalDateTime.now().minusMonths(1).toMillis())
      ),
      RecordDateField.createdAt
    )
  }

}
