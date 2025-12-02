package org.migor.feedless.repository

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import com.netflix.graphql.dgs.DgsMutation
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import graphql.schema.DataFetchingEnvironment
import kotlinx.coroutines.coroutineScope
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.PageableRequest
import org.migor.feedless.api.fromDto
import org.migor.feedless.api.mapper.toDto
import org.migor.feedless.api.throttle.Throttled
import org.migor.feedless.capability.CapabilityId
import org.migor.feedless.capability.CapabilityService
import org.migor.feedless.capability.UserCapability
import org.migor.feedless.common.PropertyService
import org.migor.feedless.document.DocumentRepository
import org.migor.feedless.document.toDomain
import org.migor.feedless.document.toDomainStringFilter
import org.migor.feedless.generated.DgsConstants
import org.migor.feedless.generated.types.CountRepositoriesInput
import org.migor.feedless.generated.types.Cursor
import org.migor.feedless.generated.types.Harvest
import org.migor.feedless.generated.types.RepositoriesInput
import org.migor.feedless.generated.types.RepositoryCreateInput
import org.migor.feedless.generated.types.RepositoryUniqueWhereInput
import org.migor.feedless.generated.types.RepositoryUpdateInput
import org.migor.feedless.generated.types.RepositoryWhereInput
import org.migor.feedless.generated.types.SourceOrderByInput
import org.migor.feedless.generated.types.SourcesWhereInput
import org.migor.feedless.source.SourceId
import org.migor.feedless.source.SourceOrderBy
import org.migor.feedless.source.SourceRepository
import org.migor.feedless.source.SourcesFilter
import org.migor.feedless.user.UserId
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.security.access.prepost.PreAuthorize
import org.migor.feedless.generated.types.Repository as RepositoryDto
import org.migor.feedless.generated.types.Source as SourceDto


fun Cursor.toPageable(maxPageSize: Int? = null): Pageable {
  val pageNumber = page.coerceAtLeast(0)
  val pageSize =
    (pageSize ?: PropertyService.maxPageSize).coerceAtLeast(1)
      .coerceAtMost(maxPageSize ?: PropertyService.maxPageSize)
  return PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"))
}

@DgsComponent
@Profile("${AppProfiles.repository} & ${AppLayer.api}")
class RepositoryResolver(
  private val repositoryUseCase: RepositoryUseCase,
  private val repositoryRepository: RepositoryRepository,
  private val sourceRepository: SourceRepository,
  private val documentRepository: DocumentRepository,
  private val harvestService: HarvestService,
  private val capabilityService: CapabilityService
) {

  private val log = LoggerFactory.getLogger(RepositoryResolver::class.simpleName)

  @Throttled
  @DgsQuery
  suspend fun repositories(
    dfe: DataFetchingEnvironment,
    @InputArgument data: RepositoriesInput,
  ): List<RepositoryDto> = coroutineScope {
    log.debug("repositories $data")

    val userId = userId()!!
    val pageable = data.cursor.toPageable().toPageableRequest()
    if (pageable.pageSize == 0) {
      emptyList()
    } else {
      val whereFilter = data.where?.toDomain()
      if (data.capability == null) {
        repositoryUseCase.findAllByUserId(pageable, whereFilter, userId)
          .map { it.toDto(it.ownerId == userId.uuid) }
      } else {
        val capabilityId = CapabilityId(data.capability!!)
        if (!capabilityService.hasCapability(capabilityId)) {
          throw IllegalArgumentException("Capability ${data.capability} is not present")
        }
        val capability = capabilityService.getCapability(capabilityId)!!
        repositoryUseCase.provideAll(capability, pageable, whereFilter)
          .map { it.toDto(it.ownerId == userId.uuid) }
      }
    }
  }

  @Throttled
  @DgsQuery(field = DgsConstants.QUERY.CountRepositories)
  suspend fun countRepositories(
    dfe: DataFetchingEnvironment,
    @InputArgument(DgsConstants.QUERY.COUNTREPOSITORIES_INPUT_ARGUMENT.Data) data: CountRepositoriesInput,
  ): Int = coroutineScope {
    log.debug("countRepositories")
    repositoryUseCase.countAll(userId(), data.product.fromDto())
  }

  private fun userId(): UserId? {
    return capabilityService.getCapability(UserCapability.ID)?.let { UserCapability.resolve(it) }
  }

  @Throttled
  @DgsQuery(field = DgsConstants.QUERY.Repository)
  suspend fun repository(
    dfe: DataFetchingEnvironment,
    @InputArgument(DgsConstants.QUERY.REPOSITORY_INPUT_ARGUMENT.Data) data: RepositoryWhereInput,
  ): RepositoryDto = coroutineScope {
    log.debug("repository $data")
    val repository = repositoryRepository.findById(RepositoryId(data.where.id))
      ?: throw IllegalArgumentException("Repository not found")
    repository.toDto(repository.ownerId == userId()?.uuid)
  }

  @Throttled
  @DgsMutation(field = DgsConstants.MUTATION.CreateRepositories)
  @PreAuthorize("@capabilityService.hasToken()")
  suspend fun createRepositories(
    dfe: DataFetchingEnvironment,
    @InputArgument(DgsConstants.MUTATION.CREATEREPOSITORIES_INPUT_ARGUMENT.Data) data: List<RepositoryCreateInput>,
  ): List<RepositoryDto> = coroutineScope {
    log.debug("createRepositories $data")
    repositoryUseCase.create(data).map { it.toDto(it.ownerId == userId()?.uuid) }
  }

  @Throttled
  @DgsMutation(field = DgsConstants.MUTATION.UpdateRepository)
  @PreAuthorize("@capabilityService.hasCapability('user')")
  suspend fun updateRepository(
    dfe: DataFetchingEnvironment,
    @InputArgument(DgsConstants.MUTATION.UPDATEREPOSITORY_INPUT_ARGUMENT.Data) data: RepositoryUpdateInput,
  ) = coroutineScope {
    log.debug("updateRepository $data")
    repositoryUseCase.updateRepository(RepositoryId(data.where.id), data.data, userId()!!)
    true
  }

  @Throttled
  @DgsMutation(field = DgsConstants.MUTATION.DeleteRepository)
  @PreAuthorize("@capabilityService.hasCapability('user')")
  suspend fun deleteRepository(
    dfe: DataFetchingEnvironment,
    @InputArgument(DgsConstants.MUTATION.DELETEREPOSITORY_INPUT_ARGUMENT.Data) data: RepositoryUniqueWhereInput,
  ): Boolean = coroutineScope {
    log.debug("deleteRepository $data")
    repositoryUseCase.delete(RepositoryId(data.id))
    true
  }


  @DgsData(parentType = DgsConstants.REPOSITORY.TYPE_NAME, field = DgsConstants.REPOSITORY.Sources)
  suspend fun sources(
    @InputArgument(DgsConstants.REPOSITORY.SOURCES_INPUT_ARGUMENT.Cursor) cursor: Cursor,
    @InputArgument(DgsConstants.REPOSITORY.SOURCES_INPUT_ARGUMENT.Where) where: SourcesWhereInput?,
    @InputArgument(DgsConstants.REPOSITORY.SOURCES_INPUT_ARGUMENT.Order) order: List<SourceOrderByInput>?,
    dfe: DgsDataFetchingEnvironment,
  ): List<SourceDto> = coroutineScope {
    val repository: RepositoryDto = dfe.getSourceOrThrow()
    if (repository.currentUserIsOwner) {
      val pageable = cursor.toPageable(10)
      if (pageable.pageSize == 0) {
        emptyList()
      } else {
        sourceRepository.findAllByRepositoryIdFiltered(
          RepositoryId(repository.id),
          pageable.toPageableRequest(),
          where?.toDomain(),
          order?.map { it.toDomain() }
        )
          .toList()
          .map { it.toDto() }
      }
    } else {
      emptyList()
    }
  }

  @DgsData(parentType = DgsConstants.REPOSITORY.TYPE_NAME, field = DgsConstants.REPOSITORY.SourcesCount)
  suspend fun sourcesCount(
    dfe: DgsDataFetchingEnvironment,
  ): Long = coroutineScope {
    val repository: RepositoryDto = dfe.getSourceOrThrow()
    if (repository.currentUserIsOwner) {
      sourceRepository.countByRepositoryId(RepositoryId(repository.id))
    } else {
      0
    }
  }

  @DgsData(parentType = DgsConstants.SOURCE.TYPE_NAME, field = DgsConstants.SOURCE.RecordCount)
  suspend fun recordCountForSources(
    dfe: DgsDataFetchingEnvironment,
  ): Int = coroutineScope {
    val source: SourceDto = dfe.getSourceOrThrow()
    documentRepository.countBySourceId(SourceId(source.id))
  }

  @DgsData(parentType = DgsConstants.SOURCE.TYPE_NAME, field = DgsConstants.SOURCE.Harvests)
  suspend fun harvests(
    dfe: DgsDataFetchingEnvironment,
  ): List<Harvest> = coroutineScope {
    val source: SourceDto = dfe.getSourceOrThrow()
    harvestService.lastHarvests(SourceId(source.id)).map { it.toDto() }
  }

  @DgsData(parentType = DgsConstants.REPOSITORY.TYPE_NAME, field = DgsConstants.REPOSITORY.SourcesCountWithProblems)
  suspend fun sourcesCountWithProblems(
    dfe: DgsDataFetchingEnvironment,
  ): Int = coroutineScope {
    val repository: RepositoryDto = dfe.getSourceOrThrow()
    sourceRepository.countSourcesWithProblems(RepositoryId(repository.id))
  }

  @DgsData(parentType = DgsConstants.REPOSITORY.TYPE_NAME, field = DgsConstants.REPOSITORY.Tags)
  suspend fun tags(dfe: DgsDataFetchingEnvironment): List<String> = coroutineScope {
    val repository: RepositoryDto = dfe.getSourceOrThrow()
    sourceRepository.findAllByRepositoryIdFiltered(RepositoryId(repository.id), PageableRequest(0, 10))
      .mapNotNull { it.tags?.asList() }
      .flatten()
      .distinct()
  }
}

fun SourceOrderByInput.toDomain(): SourceOrderBy {
  return SourceOrderBy(
    title = title?.toDomain(),
    lastRecordsRetrieved = lastRecordsRetrieved?.toDomain(),
    lastRefreshedAt = lastRefreshedAt?.toDomain()
  )
}

fun SourcesWhereInput.toDomain(): SourcesFilter {
  return SourcesFilter(
    id = id?.toDomainStringFilter(),
    latLng = latLng?.toDomain(),
    like = like,
    disabled = disabled,
  )
}

fun org.migor.feedless.generated.types.RepositoriesWhereInput.toDomain(): RepositoriesFilter {
  return RepositoriesFilter(
    product = this.product?.toDomain(),
    visibility = this.visibility?.toDomain(),
    text = this.text?.toDomain(),
    tags = this.tags?.toDomain(),
  )
}

fun org.migor.feedless.generated.types.VerticalFilter.toDomain(): VerticalFilter {
  return VerticalFilter(
    eq = this.eq?.fromDto(),
    `in` = this.`in`?.map { it.fromDto() },
  )
}

fun org.migor.feedless.generated.types.VisibilityFilter.toDomain(): VisibilityFilter {
  return VisibilityFilter(
    `in` = this.`in`?.map { it.fromDto() },
  )
}

fun org.migor.feedless.generated.types.FulltextQueryFilter.toDomain(): FulltextQueryFilter {
  return FulltextQueryFilter(
    query = this.query,
  )
}

fun org.migor.feedless.generated.types.StringArrayFilter.toDomain(): StringArrayFilter {
  return StringArrayFilter(
    every = this.every,
    some = this.some,
  )
}

//private fun VoteEntity.toDto(): Annotation {
//  val annotateBool = { value: Boolean ->
//    if (value) {
//      BoolAnnotation(value)
//    } else {
//      null
//    }
//  }
//  return Annotation(
//    id = id.toString(),
//    downVote = annotateBool(downVote),
//    flag = annotateBool(flag),
//    upVote = annotateBool(upVote)
//  )
//}
