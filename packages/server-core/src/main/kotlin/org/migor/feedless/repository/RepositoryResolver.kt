package org.migor.feedless.repository

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import com.netflix.graphql.dgs.DgsMutation
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import com.netflix.graphql.dgs.context.DgsContext
import graphql.schema.DataFetchingEnvironment
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.annotation.AnnotationService
import org.migor.feedless.api.throttle.Throttled
import org.migor.feedless.common.PropertyService
import org.migor.feedless.config.DgsCustomContext
import org.migor.feedless.data.jpa.enums.fromDto
import org.migor.feedless.generated.DgsConstants
import org.migor.feedless.generated.types.Annotations
import org.migor.feedless.generated.types.CountRepositoriesInput
import org.migor.feedless.generated.types.Cursor
import org.migor.feedless.generated.types.RepositoriesInput
import org.migor.feedless.generated.types.Repository
import org.migor.feedless.generated.types.RepositoryCreateInput
import org.migor.feedless.generated.types.RepositoryUniqueWhereInput
import org.migor.feedless.generated.types.RepositoryUpdateInput
import org.migor.feedless.generated.types.RepositoryWhereInput
import org.migor.feedless.generated.types.Source
import org.migor.feedless.generated.types.SourceOrderByInput
import org.migor.feedless.generated.types.SourcesWhereInput
import org.migor.feedless.session.injectCurrentUser
import org.migor.feedless.source.SourceService
import org.migor.feedless.source.toDto
import org.migor.feedless.user.userId
import org.migor.feedless.user.userIdOptional
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*


fun Cursor.toPageable(): Pageable {
  val pageNumber = page.coerceAtLeast(0)
  val pageSize = (pageSize ?: PropertyService.maxPageSize).coerceAtLeast(1).coerceAtMost(PropertyService.maxPageSize)
  return PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"))
}

@DgsComponent
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.repository} & ${AppLayer.api}")
class RepositoryResolver {

  private val log = LoggerFactory.getLogger(RepositoryResolver::class.simpleName)

  @Autowired
  private lateinit var repositoryService: RepositoryService

  @Autowired
  private lateinit var sourceService: SourceService

  @Throttled
  @DgsQuery
  suspend fun repositories(
    dfe: DataFetchingEnvironment,
    @InputArgument data: RepositoriesInput,
  ): List<Repository> = withContext(injectCurrentUser(currentCoroutineContext(), dfe)) {
    log.debug("repositories $data")

    val userId = coroutineContext.userId()
    val pageable = data.cursor.toPageable()
    if (pageable.pageSize == 0) {
      emptyList()
    } else {
      repositoryService.findAll(data.cursor.toPageable(), data.where, userId)
        .map { it.toDto(it.ownerId == userId) }
    }
  }

  @Throttled
  @DgsQuery(field = DgsConstants.QUERY.CountRepositories)
  suspend fun countRepositories(
    dfe: DataFetchingEnvironment,
    @InputArgument(DgsConstants.QUERY.COUNTREPOSITORIES_INPUT_ARGUMENT.Data) data: CountRepositoriesInput,
  ): Int = withContext(injectCurrentUser(currentCoroutineContext(), dfe)) {
    log.debug("countRepositories")
    repositoryService.countAll(coroutineContext.userId(), data.product.fromDto())
  }

  @Throttled
  @DgsQuery(field = DgsConstants.QUERY.Repository)
  suspend fun repository(
    dfe: DataFetchingEnvironment,
    @InputArgument(DgsConstants.QUERY.REPOSITORY_INPUT_ARGUMENT.Data) data: RepositoryWhereInput,
  ): Repository = withContext(injectCurrentUser(currentCoroutineContext(), dfe)) {
    log.debug("repository $data")
    val repository = repositoryService.findById(UUID.fromString(data.where.id)).orElseThrow()
    repository.toDto(repository.ownerId == coroutineContext.userIdOptional())
  }

  @Throttled
  @DgsMutation(field = DgsConstants.MUTATION.CreateRepositories)
  @PreAuthorize("hasAuthority('ANONYMOUS')")
  suspend fun createRepositories(
    dfe: DataFetchingEnvironment,
    @InputArgument(DgsConstants.MUTATION.CREATEREPOSITORIES_INPUT_ARGUMENT.Data) data: List<RepositoryCreateInput>,
  ): List<Repository> = withContext(injectCurrentUser(currentCoroutineContext(), dfe)) {
    log.debug("createRepositories $data")
    repositoryService.create(data)
  }

  @Throttled
  @DgsMutation(field = DgsConstants.MUTATION.UpdateRepository)
  @PreAuthorize("hasAuthority('USER')")
  suspend fun updateRepository(
    dfe: DataFetchingEnvironment,
    @InputArgument(DgsConstants.MUTATION.UPDATEREPOSITORY_INPUT_ARGUMENT.Data) data: RepositoryUpdateInput,
  ) = withContext(injectCurrentUser(currentCoroutineContext(), dfe)) {
    log.debug("updateRepository $data")
    repositoryService.updateRepository(UUID.fromString(data.where.id), data.data)
    true
  }

  @Throttled
  @DgsMutation(field = DgsConstants.MUTATION.DeleteRepository)
  @PreAuthorize("hasAuthority('USER')")
  suspend fun deleteRepository(
    dfe: DataFetchingEnvironment,
    @InputArgument(DgsConstants.MUTATION.DELETEREPOSITORY_INPUT_ARGUMENT.Data) data: RepositoryUniqueWhereInput,
  ): Boolean = withContext(injectCurrentUser(currentCoroutineContext(), dfe)) {
    log.debug("deleteRepository $data")
    repositoryService.delete(UUID.fromString(data.id))
    true
  }


  @DgsData(parentType = DgsConstants.REPOSITORY.TYPE_NAME, field = DgsConstants.REPOSITORY.Sources)
  suspend fun sources(
    @InputArgument(DgsConstants.REPOSITORY.SOURCES_INPUT_ARGUMENT.Cursor) cursor: Cursor,
    @InputArgument(DgsConstants.REPOSITORY.SOURCES_INPUT_ARGUMENT.Where) where: SourcesWhereInput?,
    @InputArgument(DgsConstants.REPOSITORY.SOURCES_INPUT_ARGUMENT.OrderBy) orderBy: SourceOrderByInput?,
    dfe: DgsDataFetchingEnvironment,
  ): List<Source> = coroutineScope {
    val repository: Repository = dfe.getSource()
    if (repository.currentUserIsOwner) {
      val pageable = cursor.toPageable() ?: PageRequest.of(0, 10)
      if (pageable.pageSize == 0) {
        emptyList()
      } else {
        sourceService.findAllByRepositoryIdFiltered(UUID.fromString(repository.id), pageable, where, orderBy)
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
    val repository: Repository = dfe.getSource()
    if (repository.currentUserIsOwner) {
      sourceService.countAllByRepositoryId(UUID.fromString(repository.id))
    } else {
      0
    }
  }

  @DgsData(parentType = DgsConstants.SOURCE.TYPE_NAME, field = DgsConstants.SOURCE.RecordCount)
  suspend fun recordCountForSources(
    dfe: DgsDataFetchingEnvironment,
  ): Int = coroutineScope {
    val source: Source = dfe.getSource()
    sourceService.countDocumentsBySourceId(UUID.fromString(source.id))
  }

//  @DgsData(parentType = DgsConstants.REPOSITORY.TYPE_NAME, field = DgsConstants.REPOSITORY.Harvests)
//  suspend fun harvests(
//    dfe: DgsDataFetchingEnvironment,
//  ): List<Harvest> = coroutineScope {
//    emptyList()
//    val repository: Repository = dfe.getSource()
//    if (repository.currentUserIsOwner) {
//      val harvests = withContext(Dispatchers.IO) {
//        val pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt"))
//        harvestDAO.findAllByRepositoryId(UUID.fromString(repository.id), pageable).map { it.toDto() }
//      }
//      harvests
//    } else {
//      emptyList()
//    }
//  }

  @DgsData(parentType = DgsConstants.REPOSITORY.TYPE_NAME, field = DgsConstants.REPOSITORY.SourcesCountWithProblems)
  suspend fun sourcesCountWithProblems(
    dfe: DgsDataFetchingEnvironment,
  ): Int = coroutineScope {
    val repository: Repository = dfe.getSource()
    sourceService.countProblematicSourcesByRepositoryId(UUID.fromString(repository.id))
  }

  @DgsData(parentType = DgsConstants.REPOSITORY.TYPE_NAME, field = DgsConstants.REPOSITORY.Tags)
  suspend fun tags(dfe: DgsDataFetchingEnvironment): List<String> = coroutineScope {
    val repository: Repository = dfe.getSource()
    sourceService.findAllByRepositoryIdFiltered(UUID.fromString(repository.id), PageRequest.of(0, 10))
      .mapNotNull { it.tags?.asList() }
      .flatten()
      .distinct()
  }
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
