package org.migor.feedless.plan

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsQuery
import kotlinx.coroutines.coroutineScope
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.ApiParams
import org.migor.feedless.api.throttle.Throttled
import org.migor.feedless.data.jpa.enums.ProductName
import org.migor.feedless.generated.types.Feature
import org.migor.feedless.session.SessionService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.RequestHeader

@DgsComponent
@Profile(AppProfiles.database)
class FeatureResolver {

  private val log = LoggerFactory.getLogger(FeatureResolver::class.simpleName)

  @Autowired
  lateinit var featureService: FeatureService

  @Autowired
  lateinit var sessionService: SessionService

  @Throttled
  @DgsQuery
  @PreAuthorize("hasAuthority('USER')")
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun features(@RequestHeader(ApiParams.corrId) corrId: String): List<Feature> =
    coroutineScope {
      log.info("[$corrId] features")
      if (!sessionService.user(corrId).root) {
        throw IllegalArgumentException("user must be root")
      }
      featureService.findAllByProduct(ProductName.system)
    }

//  @Throttled
//  @DgsMutation
//  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
//  suspend fun updateFeature(@RequestHeader(ApiParams.corrId) corrId: String,
//                            @InputArgument data: UpdateFeatureInput
//  ): Feature =
//    coroutineScope {
//      log.info("[$corrId] updateFeature $data")
//      featureService.updateFeature(data.name, data.value.numVal?.value, data.value.boolVal?.value)
//    }
}
