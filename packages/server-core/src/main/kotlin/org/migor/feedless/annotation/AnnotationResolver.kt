package org.migor.feedless.annotation

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsMutation
import com.netflix.graphql.dgs.InputArgument
import kotlinx.coroutines.coroutineScope
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.ApiParams
import org.migor.feedless.api.throttle.Throttled
import org.migor.feedless.generated.types.CreateAnnotationInput
import org.migor.feedless.generated.types.DeleteAnnotationInput
import org.migor.feedless.util.CryptUtil.handleCorrId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.RequestHeader

@DgsComponent
@Profile("${AppProfiles.agent} & ${AppProfiles.api}")
class AnnotationResolver {

  private val log = LoggerFactory.getLogger(AnnotationResolver::class.simpleName)

  @Autowired
  private lateinit var annotationService: AnnotationService

  @Throttled
  @DgsMutation
  @PreAuthorize("hasAuthority('USER')")
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun createAnnotation(
    @RequestHeader(ApiParams.corrId) corrIdParam: String,
    @InputArgument data: CreateAnnotationInput
  ): Annotation = coroutineScope {
    val corrId = handleCorrId(corrIdParam)
    log.info("[$corrId] createAnnotation $data")
    annotationService.createAnnotation(corrId, data).toDto()
  }

  @Throttled
  @DgsMutation
  @PreAuthorize("hasAuthority('USER')")
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun deleteAnnotation(
    @RequestHeader(ApiParams.corrId) corrIdParam: String,
    @InputArgument data: DeleteAnnotationInput
  ): Boolean = coroutineScope {
    val corrId = handleCorrId(corrIdParam)
    log.info("[$corrId] deleteAnnotation $data")
    annotationService.deleteAnnotation(corrId, data)
  }
}

private fun AnnotationEntity.toDto(): Annotation {
  TODO("Not yet implemented")
}
