package org.migor.feedless.annotation

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import com.netflix.graphql.dgs.DgsMutation
import com.netflix.graphql.dgs.InputArgument
import com.netflix.graphql.dgs.context.DgsContext
import graphql.schema.DataFetchingEnvironment
import kotlinx.coroutines.coroutineScope
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.throttle.Throttled
import org.migor.feedless.config.DgsCustomContext
import org.migor.feedless.generated.DgsConstants
import org.migor.feedless.generated.types.CreateAnnotationInput
import org.migor.feedless.generated.types.DeleteAnnotationInput
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.session.SessionService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*
import org.migor.feedless.generated.types.Annotation as AnnotationDto
import org.migor.feedless.generated.types.Annotations as AnnotationsDto
import org.migor.feedless.generated.types.Repository as RepositoryDto

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
  @PreAuthorize("@capabilityService.hasCapability('user')")
  suspend fun createAnnotation(
    dfe: DataFetchingEnvironment,
    @InputArgument(DgsConstants.MUTATION.CREATEANNOTATION_INPUT_ARGUMENT.Data) data: CreateAnnotationInput
  ): AnnotationDto = coroutineScope {
    log.debug("createAnnotation $data")
    annotationService.createAnnotation(data, sessionService.user()).toDto()
  }

  @Throttled
  @DgsMutation(field = DgsConstants.MUTATION.DeleteAnnotation)
  @PreAuthorize("@capabilityService.hasCapability('user')")
  suspend fun deleteAnnotation(
    dfe: DataFetchingEnvironment,
    @InputArgument(DgsConstants.MUTATION.DELETEANNOTATION_INPUT_ARGUMENT.Data) data: DeleteAnnotationInput,
  ): Boolean = coroutineScope {
    log.debug("deleteAnnotation $data")
    annotationService.deleteAnnotation(data, sessionService.user())
    true
  }

  @DgsData(parentType = DgsConstants.ANNOTATIONS.TYPE_NAME, field = DgsConstants.ANNOTATIONS.Votes)
  suspend fun votes(
    dfe: DgsDataFetchingEnvironment
  ): List<AnnotationDto> = coroutineScope {
    val context = DgsContext.getCustomContext<DgsCustomContext>(dfe)
    val userId = context.userId
    userId?.let {
      context.repositoryId?.let { repositoryId ->
        annotationService.findAllVotesByUserIdAndRepositoryId(userId, repositoryId).map { it.toDto() }
      } ?: annotationService.findAllVotesByUserIdAndDocumentId(userId, context.documentId!!)
        .map { it.toDto() }
    } ?: emptyList()
  }

  @DgsData(parentType = DgsConstants.REPOSITORY.TYPE_NAME, field = DgsConstants.REPOSITORY.Annotations)
  suspend fun annotations(
    dfe: DgsDataFetchingEnvironment
  ): AnnotationsDto = coroutineScope {
    val repository: RepositoryDto = dfe.getSourceOrThrow()

    val repositoryId = RepositoryId(UUID.fromString(repository.id))
    DgsContext.getCustomContext<DgsCustomContext>(dfe).repositoryId = repositoryId
    AnnotationsDto(
      upVotes = annotationService.countUpVotesByRepositoryId(repositoryId),
      downVotes = annotationService.countDownVotesByRepositoryId(repositoryId)
    )
  }
}

