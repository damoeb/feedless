package org.migor.feedless.annotation

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsMutation
import com.netflix.graphql.dgs.InputArgument
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.ApiParams
import org.migor.feedless.api.throttle.Throttled
import org.migor.feedless.generated.DgsConstants
import org.migor.feedless.generated.types.CreateAnnotationInput
import org.migor.feedless.generated.types.DeleteAnnotationInput
import org.migor.feedless.session.useRequestContext
import org.migor.feedless.util.CryptUtil.handleCorrId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.RequestHeader

@DgsComponent
@Profile("${AppProfiles.annotation} & ${AppLayer.api}")
class AnnotationResolver {

  private val log = LoggerFactory.getLogger(AnnotationResolver::class.simpleName)

  @Autowired
  private lateinit var annotationService: AnnotationService

  @Throttled
  @DgsMutation(field = DgsConstants.MUTATION.CreateAnnotation)
  @PreAuthorize("hasAuthority('USER')")
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun createAnnotation(
    @RequestHeader(ApiParams.corrId) corrIdParam: String,
    @InputArgument data: CreateAnnotationInput
  ): Annotation = withContext(useRequestContext(currentCoroutineContext())) {
    val corrId = handleCorrId(corrIdParam)
    log.debug("[$corrId] createAnnotation $data")
    annotationService.createAnnotation(corrId, data).toDto()
  }

  @Throttled
  @DgsMutation(field = DgsConstants.MUTATION.DeleteAnnotation)
  @PreAuthorize("hasAuthority('USER')")
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun deleteAnnotation(
    @RequestHeader(ApiParams.corrId) corrIdParam: String,
    @InputArgument data: DeleteAnnotationInput
  ): Boolean = withContext(useRequestContext(currentCoroutineContext())) {
    val corrId = handleCorrId(corrIdParam)
    log.debug("[$corrId] deleteAnnotation $data")
    annotationService.deleteAnnotation(corrId, data)
  }
}

private fun AnnotationEntity.toDto(): Annotation {
  TODO("Not yet implemented")
}
