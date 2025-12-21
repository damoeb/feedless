package org.migor.feedless.document

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import com.netflix.graphql.dgs.DgsMutation
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import com.netflix.graphql.dgs.context.DgsContext
import graphql.schema.DataFetchingEnvironment
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.NotFoundException
import org.migor.feedless.api.throttle.Throttled
import org.migor.feedless.api.toDto
import org.migor.feedless.common.PropertyService
import org.migor.feedless.config.DgsCustomContext
import org.migor.feedless.generated.DgsConstants
import org.migor.feedless.generated.types.CreateRecordInput
import org.migor.feedless.generated.types.DeleteRecordsInput
import org.migor.feedless.generated.types.Record
import org.migor.feedless.generated.types.RecordDateField
import org.migor.feedless.generated.types.RecordFrequency
import org.migor.feedless.generated.types.RecordWhereInput
import org.migor.feedless.generated.types.RecordsInput
import org.migor.feedless.generated.types.Repository
import org.migor.feedless.generated.types.UpdateRecordInput
import org.migor.feedless.pipeline.plugins.StringFilter
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.repository.RepositoryUseCase
import org.migor.feedless.repository.toPageable
import org.migor.feedless.repository.toPageableRequest
import org.migor.feedless.session.createRequestContext
import org.migor.feedless.source.SourceId
import org.migor.feedless.util.toLocalDateTime
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.security.access.prepost.PreAuthorize
import java.time.LocalDateTime
import org.migor.feedless.generated.types.DatesWhereInput as DatesWhereInputDto
import org.migor.feedless.generated.types.RecordOrderByInput as RecordOrderByInputDto
import org.migor.feedless.generated.types.RecordsWhereInput as RecordsWhereInputDto
import org.migor.feedless.generated.types.RepositoryUniqueWhereInput as RepositoryUniqueWhereInputDto
import org.migor.feedless.generated.types.StringFilterInput as StringFilterInputDto

@DgsComponent
@Profile("${AppProfiles.document} & ${AppLayer.api}")
class DocumentResolver(
  private val repositoryUseCase: RepositoryUseCase,
  private val propertyService: PropertyService,
  private val documentUseCase: DocumentUseCase,
  private val documentGuard: DocumentGuard
) {

  private val log = LoggerFactory.getLogger(DocumentResolver::class.simpleName)

  @Throttled
  @DgsQuery(field = DgsConstants.QUERY.Record)
  suspend fun record(
    dfe: DataFetchingEnvironment,
    @InputArgument(DgsConstants.QUERY.RECORD_INPUT_ARGUMENT.Data) data: RecordWhereInput,
  ): Record = withContext(context = createRequestContext()) {
    log.debug("record $data")
    val documentId = DocumentId(data.where.id)
    val document = documentGuard.requireRead(documentId)

    DgsContext.getCustomContext<DgsCustomContext>(dfe).documentId = documentId

    document.toDto(propertyService)
  }

  @Throttled
  @DgsQuery(field = DgsConstants.QUERY.Records)
  suspend fun records(
    dfe: DataFetchingEnvironment,
    @InputArgument(DgsConstants.QUERY.RECORDS_INPUT_ARGUMENT.Data) data: RecordsInput,
  ): List<Record> = withContext(context = createRequestContext()) {
    log.debug("records $data")
    val repositoryId = RepositoryId(data.where.repository.id)

    val repository =
      repositoryUseCase.findById(repositoryId) ?: throw NotFoundException("repository $repositoryId not found")
    val pageable = data.cursor.toPageable()
    if (pageable.pageSize == 0) {
      emptyList()
    } else {
      documentUseCase.findAllByRepositoryId(
        repository.id,
        data.where.toDomain(),
        data.orderBy?.toDomain(),
        pageable = pageable.toPageableRequest()
      ).map {
        it.toDto(
          propertyService
        )
      }.toList()
    }
  }

  @DgsData(parentType = DgsConstants.REPOSITORY.TYPE_NAME, field = DgsConstants.REPOSITORY.DocumentCount)
  suspend fun documentCount(dfe: DgsDataFetchingEnvironment): Long = coroutineScope {
    val repository: Repository = dfe.getSourceOrThrow()
    documentUseCase.countByRepositoryId(RepositoryId(repository.id))
  }

  @DgsMutation(field = DgsConstants.MUTATION.DeleteRecords)
  @PreAuthorize("@capabilityService.hasCapability('user')")
  suspend fun deleteRecords(
    dfe: DataFetchingEnvironment,
    @InputArgument(DgsConstants.MUTATION.DELETERECORDS_INPUT_ARGUMENT.Data) data: DeleteRecordsInput,
  ): Boolean = withContext(context = createRequestContext()) {
    documentUseCase.deleteDocuments(
      RepositoryId(data.where.repository.id),
      data.where.id!!.toDomain()
    )
    true
  }

  @DgsMutation(field = DgsConstants.MUTATION.CreateRecords)
  @PreAuthorize("@capabilityService.hasCapability('user')")
  suspend fun createRecords(
    dfe: DataFetchingEnvironment,
    @InputArgument(DgsConstants.MUTATION.CREATERECORDS_INPUT_ARGUMENT.Records) records: List<CreateRecordInput>,
  ): List<Record> = withContext(context = createRequestContext()) {
    records.map { documentUseCase.createDocument(it).toDto(propertyService) }
  }

  @DgsMutation(field = DgsConstants.MUTATION.UpdateRecord)
  @PreAuthorize("@capabilityService.hasCapability('user')")
  suspend fun updateRecord(
    dfe: DataFetchingEnvironment,
    @InputArgument(DgsConstants.MUTATION.UPDATERECORD_INPUT_ARGUMENT.Data) data: UpdateRecordInput,
  ): Boolean = withContext(context = createRequestContext()) {
    documentUseCase.updateDocument(data.data, DocumentId(data.where.id)).toDto(propertyService)
    true
  }

  @DgsQuery(field = DgsConstants.QUERY.RecordsFrequency)
  suspend fun recordsFrequency(
    @InputArgument(DgsConstants.QUERY.RECORDSFREQUENCY_INPUT_ARGUMENT.Where) where: RecordsWhereInputDto,
    @InputArgument(DgsConstants.QUERY.RECORDSFREQUENCY_INPUT_ARGUMENT.GroupBy) groupBy: RecordDateField,
  ): List<RecordFrequency> = withContext(context = createRequestContext()) {
    documentUseCase.getRecordFrequency(where.toDomain(), groupBy.toDomain()).map { it.toDto() }
  }


  @DgsData(parentType = DgsConstants.REPOSITORY.TYPE_NAME, field = DgsConstants.REPOSITORY.Frequency)
  suspend fun frequency(
    dfe: DgsDataFetchingEnvironment,
  ): List<RecordFrequency> = coroutineScope {
    val repository: Repository = dfe.getSourceOrThrow()
    documentUseCase.getRecordFrequency(
      DocumentsFilter(
        repository = RepositoryId(repository.id),
        createdAt = DatesWhereInput(after = LocalDateTime.now().minusMonths(1))
      ),
      DocumentDateField.createdAt
    ).map { it.toDto() }
  }

}


fun StringFilterInputDto.toDomain(): StringFilter {
  return StringFilter(
    eq = eq,
    `in` = `in`
  )
}

fun DocumentFrequency.toDto(): RecordFrequency {
  return RecordFrequency(
    count = count,
    group = group,
  )
}

fun RecordDateField.toDomain(): DocumentDateField {
  return when (this) {
    RecordDateField.createdAt -> DocumentDateField.createdAt
    RecordDateField.publishedAt -> DocumentDateField.publishedAt
    RecordDateField.startingAt -> DocumentDateField.startingAt
  }
}

fun RecordsWhereInputDto.toDomain(): DocumentsFilter {
  return DocumentsFilter(
    id = id?.toDomainStringFilter(),
    repository = repository.toDomain(),
    source = source?.toDomain(),
    startedAt = startedAt?.toDomain(),
    createdAt = createdAt?.toDomain(),
    publishedAt = publishedAt?.toDomain(),
    updatedAt = updatedAt?.toDomain(),
    latLng = latLng?.toDomain(),
    tags = tags?.toDomainStringFilter()
  )
}

private fun RepositoryUniqueWhereInputDto.toDomain(): RepositoryId {
  return RepositoryId(id)
}

fun org.migor.feedless.generated.types.SourceUniqueWhereInput.toDomain(): SourceUniqueWhere {
  return SourceUniqueWhere(id = SourceId(id))
}

fun StringFilterInputDto.toDomainStringFilter(): org.migor.feedless.document.StringFilter {
  return org.migor.feedless.document.StringFilter(
    eq = eq,
    `in` = `in`
  )
}

fun DatesWhereInputDto.toDomain(): DatesWhereInput {
  return DatesWhereInput(
    before = before?.toLocalDateTime(),
    after = after?.toLocalDateTime(),
    inFuture = inFuture
  )
}

fun org.migor.feedless.generated.types.GeoPointWhereInput.toDomain(): GeoPointWhereInput {
  return GeoPointWhereInput(
    near = near?.toDomain(),
    within = within?.toDomain()
  )
}

fun org.migor.feedless.generated.types.GeoPointWhereNearInput.toDomain(): GeoPointWhereNearInput {
  return GeoPointWhereNearInput(
    point = point.toDomain(),
    distanceKm = distanceKm
  )
}

fun org.migor.feedless.generated.types.GeoPointWhereWithinInput.toDomain(): GeoPointWhereWithinInput {
  return GeoPointWhereWithinInput(
    nw = nw.toDomain(),
    se = se.toDomain()
  )
}

fun org.migor.feedless.generated.types.GeoPointInput.toDomain(): GeoPointInput {
  return GeoPointInput(
    lat = lat,
    lng = lng
  )
}

fun RecordOrderByInputDto.toDomain(): RecordOrderBy {
  return RecordOrderBy(
    startedAt = startedAt?.toDomain()
  )
}

fun org.migor.feedless.generated.types.SortOrder.toDomain(): SortOrder {
  return when (this) {
    org.migor.feedless.generated.types.SortOrder.asc -> SortOrder.ASC
    else -> SortOrder.DESC
  }
}
