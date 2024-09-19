package org.migor.feedless.annotation

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsMutation
import com.netflix.graphql.dgs.InputArgument
import graphql.schema.DataFetchingEnvironment
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.throttle.Throttled
import org.migor.feedless.generated.DgsConstants
import org.migor.feedless.generated.types.Annotation
import org.migor.feedless.generated.types.BoolAnnotation
import org.migor.feedless.generated.types.CreateAnnotationInput
import org.migor.feedless.generated.types.DeleteAnnotationInput
import org.migor.feedless.generated.types.TextAnnotation
import org.migor.feedless.session.SessionService
import org.migor.feedless.session.injectCurrentUser
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@DgsComponent
@Profile("${AppProfiles.annotation} & ${AppLayer.api}")
class AnnotationResolver {

  private val log = LoggerFactory.getLogger(AnnotationResolver::class.simpleName)

  @Autowired
  private lateinit var annotationService: AnnotationService

  @Autowired
  private lateinit var sessionService: SessionService

  @Throttled
  @DgsMutation(field = DgsConstants.MUTATION.CreateAnnotation)
  @PreAuthorize("hasAuthority('USER')")
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun createAnnotation(
    dfe: DataFetchingEnvironment,
    @InputArgument data: CreateAnnotationInput
  ): Annotation = withContext(injectCurrentUser(currentCoroutineContext(), dfe)) {
    log.debug("createAnnotation $data")
    annotationService.createAnnotation(data, sessionService.user()).toDto()
  }

  @Throttled
  @DgsMutation(field = DgsConstants.MUTATION.DeleteAnnotation)
  @PreAuthorize("hasAuthority('USER')")
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun deleteAnnotation(
    dfe: DataFetchingEnvironment,
    @InputArgument data: DeleteAnnotationInput,
  ): Boolean = withContext(injectCurrentUser(currentCoroutineContext(), dfe)) {
    log.debug("deleteAnnotation $data")
    annotationService.deleteAnnotation(data, sessionService.user())
    true
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
