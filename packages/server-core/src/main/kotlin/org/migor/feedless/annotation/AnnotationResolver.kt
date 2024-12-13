package org.migor.feedless.annotation

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import com.netflix.graphql.dgs.DgsMutation
import com.netflix.graphql.dgs.InputArgument
import com.netflix.graphql.dgs.context.DgsContext
import graphql.schema.DataFetchingEnvironment
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.throttle.Throttled
import org.migor.feedless.config.DgsCustomContext
import org.migor.feedless.generated.DgsConstants
import org.migor.feedless.generated.types.Annotation
import org.migor.feedless.generated.types.Annotations
import org.migor.feedless.generated.types.BoolAnnotation
import org.migor.feedless.generated.types.CreateAnnotationInput
import org.migor.feedless.generated.types.DeleteAnnotationInput
import org.migor.feedless.generated.types.Repository
import org.migor.feedless.generated.types.TextAnnotation
import org.migor.feedless.session.SessionService
import org.migor.feedless.session.injectCurrentUser
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@DgsComponent
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.annotation} & ${AppLayer.api}")
class AnnotationResolver(
  private val annotationService: AnnotationService,
  private val sessionService: SessionService
) {

  private val log = LoggerFactory.getLogger(AnnotationResolver::class.simpleName)

  @Throttled
  @DgsMutation(field = DgsConstants.MUTATION.CreateAnnotation)
  @PreAuthorize("hasAuthority('USER')")
  suspend fun createAnnotation(
    dfe: DataFetchingEnvironment,
    @InputArgument(DgsConstants.MUTATION.CREATEANNOTATION_INPUT_ARGUMENT.Data) data: CreateAnnotationInput
  ): Annotation = withContext(injectCurrentUser(currentCoroutineContext(), dfe)) {
    log.debug("createAnnotation $data")
    annotationService.createAnnotation(data, sessionService.user()).toDto()
  }

  @Throttled
  @DgsMutation(field = DgsConstants.MUTATION.DeleteAnnotation)
  @PreAuthorize("hasAuthority('USER')")
  suspend fun deleteAnnotation(
    dfe: DataFetchingEnvironment,
    @InputArgument(DgsConstants.MUTATION.DELETEANNOTATION_INPUT_ARGUMENT.Data) data: DeleteAnnotationInput,
  ): Boolean = withContext(injectCurrentUser(currentCoroutineContext(), dfe)) {
    log.debug("deleteAnnotation $data")
    annotationService.deleteAnnotation(data, sessionService.user())
    true
  }

  @DgsData(parentType = DgsConstants.ANNOTATIONS.TYPE_NAME, field = DgsConstants.ANNOTATIONS.Votes)
  suspend fun votes(
    dfe: DgsDataFetchingEnvironment
  ): List<Annotation> = coroutineScope {
    val context = DgsContext.getCustomContext<DgsCustomContext>(dfe)
    val userId = context.userId
    userId?.let {
      context.repositoryId?.let { repositoryId ->
        annotationService.findAllVotesByUserIdAndRepositoryId(userId, repositoryId).map { it.toDto() }
      } ?: annotationService.findAllVotesByUserIdAndDocumentId(userId, context.documentId!!).map { it.toDto() }
    } ?: emptyList()
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

private fun AnnotationEntity.toDto(): Annotation {
  return if (this is TextAnnotationEntity) {
    Annotation(
      id = id.toString(),
      text = TextAnnotation(
        fromChar = fromChar,
        toChar = toChar,
      )
    )
  } else {
    if (this is VoteEntity) {
      val toBoolAnnotation = { value: Boolean ->
        if (value) {
          BoolAnnotation(value)
        } else {
          null
        }
      }

      Annotation(
        id = id.toString(),
        flag = toBoolAnnotation(flag),
        upVote = toBoolAnnotation(upVote),
        downVote = toBoolAnnotation(downVote),
      )
    } else {
      Annotation(id = id.toString())
    }
  }
}
