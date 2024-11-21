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
import org.migor.feedless.session.injectCurrentUser
import org.migor.feedless.source.SourceService
import org.migor.feedless.user.userId
import org.migor.feedless.user.userIdOptional
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*


fun handleCursor(cursor: Cursor): Pair<Int, Int> {
  val pageNumber = handlePageNumber(cursor.page).coerceAtLeast(0)
  val pageSize = handlePageSize(cursor.pageSize)
  return Pair(pageNumber, pageSize)
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

  @Autowired
  private lateinit var annotationService: AnnotationService

  @Throttled
  @DgsQuery
  suspend fun repositories(
    dfe: DataFetchingEnvironment,
    @InputArgument data: RepositoriesInput,
  ): List<Repository> = withContext(injectCurrentUser(currentCoroutineContext(), dfe)) {
    log.debug("repositories $data")

    val (pageNumber, pageSize) = handleCursor(data.cursor)

    val userId = coroutineContext.userId()
    repositoryService.findAll(pageNumber, pageSize, data.where, userId)
      .map { it.toDto(it.ownerId == userId) }
  }

  @Throttled
  @DgsQuery
  suspend fun countRepositories(
    dfe: DataFetchingEnvironment,
    @InputArgument data: CountRepositoriesInput,
  ): Int = withContext(injectCurrentUser(currentCoroutineContext(), dfe)) {
    log.debug("countRepositories")
    repositoryService.countAll(coroutineContext.userId(), data.product.fromDto())
  }

  @Throttled
  @DgsQuery
  suspend fun repository(
    dfe: DataFetchingEnvironment,
    @InputArgument data: RepositoryWhereInput,
  ): Repository = withContext(injectCurrentUser(currentCoroutineContext(), dfe)) {
    log.debug("repository $data")
    val repository = repositoryService.findById(UUID.fromString(data.where.id))
    repository.toDto(repository.ownerId == coroutineContext.userIdOptional())
  }

  @Throttled
  @DgsMutation(field = DgsConstants.MUTATION.CreateRepositories)
  @PreAuthorize("hasAuthority('ANONYMOUS')")
  suspend fun createRepositories(
    dfe: DataFetchingEnvironment,
    @InputArgument("data") data: List<RepositoryCreateInput>,
  ): List<Repository> = withContext(injectCurrentUser(currentCoroutineContext(), dfe)) {
    log.debug("createRepositories $data")
    repositoryService.create(data)
  }

  @Throttled
  @DgsMutation(field = DgsConstants.MUTATION.UpdateRepository)
  @PreAuthorize("hasAuthority('USER')")
  suspend fun updateRepository(
    dfe: DataFetchingEnvironment,
    @InputArgument("data") data: RepositoryUpdateInput,
  ): Repository = withContext(injectCurrentUser(currentCoroutineContext(), dfe)) {
    log.debug("updateRepository $data")
    repositoryService.update(UUID.fromString(data.where.id), data.data).toDto(true)
  }

  @Throttled
  @DgsMutation(field = DgsConstants.MUTATION.DeleteRepository)
  @PreAuthorize("hasAuthority('USER')")
  suspend fun deleteRepository(
    dfe: DataFetchingEnvironment,
    @InputArgument("data") data: RepositoryUniqueWhereInput,
  ): Boolean = withContext(injectCurrentUser(currentCoroutineContext(), dfe)) {
    log.debug("deleteRepository $data")
    repositoryService.delete(UUID.fromString(data.id))
    true
  }


  @DgsData(parentType = DgsConstants.REPOSITORY.TYPE_NAME, field = DgsConstants.REPOSITORY.Sources)
  suspend fun sources(
    dfe: DgsDataFetchingEnvironment,
  ): List<Source> = coroutineScope {
    val repository: Repository = dfe.getSource()
    if (repository.currentUserIsOwner) {
      sourceService.findAllByRepositoryId(UUID.fromString(repository.id))
        .map { it.toScrapeRequest() }
    } else {
      emptyList()
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

  @DgsData(parentType = DgsConstants.REPOSITORY.TYPE_NAME, field = DgsConstants.REPOSITORY.HasDisabledSources)
  suspend fun hasDisabledSources(
    dfe: DgsDataFetchingEnvironment,
  ): Boolean = coroutineScope {
    val repository: Repository = dfe.getSource()
    sourceService.existsByRepositoryIdAndDisabledTrue(UUID.fromString(repository.id))
  }

  @DgsData(parentType = DgsConstants.REPOSITORY.TYPE_NAME, field = DgsConstants.REPOSITORY.Tags)
  suspend fun tags(dfe: DgsDataFetchingEnvironment): List<String> = coroutineScope {
    val repository: Repository = dfe.getSource()
    sourceService.findAllByRepositoryId(UUID.fromString(repository.id))
      .mapNotNull { it.tags?.asList() }
      .flatten()
      .distinct()
  }


  @DgsData(parentType = DgsConstants.REPOSITORY.TYPE_NAME, field = DgsConstants.REPOSITORY.Annotations)
  suspend fun annotations(
    dfe: DgsDataFetchingEnvironment
  ): Annotations = coroutineScope {
    val repository: Repository = dfe.getSource()

    val repositoryId = UUID.fromString(repository.id)
    DgsContext.getCustomContext<DgsCustomContext>(dfe).repositoryId = repositoryId
    Annotations(
      upVotes = annotationService.countUpVotesByRepositoryId(repositoryId),
      downVotes = annotationService.countDownVotesByRepositoryId(repositoryId)
    )
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

private fun handlePageNumber(page: Int?): Int =
  page ?: 0

private fun handlePageSize(pageSize: Int?): Int =
  (pageSize ?: PropertyService.maxPageSize).coerceAtLeast(1).coerceAtMost(PropertyService.maxPageSize)
