package org.migor.feedless.plan

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsMutation
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.ApiParams
import org.migor.feedless.api.throttle.Throttled
import org.migor.feedless.generated.DgsConstants
import org.migor.feedless.generated.types.FeatureGroup
import org.migor.feedless.generated.types.FeatureGroupWhereInput
import org.migor.feedless.generated.types.UpdateFeatureValueInput
import org.migor.feedless.session.SessionService
import org.migor.feedless.session.useRequestContext
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.RequestHeader
import java.util.*

@DgsComponent
@Profile("${AppProfiles.database} & ${AppProfiles.api}")
class FeatureResolver {

  private val log = LoggerFactory.getLogger(FeatureResolver::class.simpleName)

  @Autowired
  private lateinit var featureService: FeatureService

  @Autowired
  private lateinit var sessionService: SessionService

  @Throttled
  @DgsQuery
//  @PreAuthorize("hasAuthority('USER')")
  @Transactional
  suspend fun featureGroups(
    @RequestHeader(ApiParams.corrId) corrId: String,
    @InputArgument inherit: Boolean,
    @InputArgument where: FeatureGroupWhereInput,

    ): List<FeatureGroup> =
    withContext(useRequestContext(currentCoroutineContext())) {
      log.debug("[$corrId] featureGroups inherit=$inherit where=$where")
//      if (!sessionService.user(corrId).root) {
//        throw IllegalArgumentException("user must be root")
//      }
      featureService.findAllGroups(inherit, where)
    }

//  @DgsData(parentType = DgsConstants.FEATUREGROUP.TYPE_NAME)
//  @Transactional(propagation = Propagation.REQUIRED)
//  suspend fun features(dfe: DgsDataFetchingEnvironment): List<Feature> = coroutineScope {
//    val group: FeatureGroup = dfe.getSource()!!
//    featureService.findAllByGroupId(UUID.fromString(group.id))
//  }

  @Throttled
  @DgsMutation(field = DgsConstants.MUTATION.UpdateFeatureValue)
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun updateFeatureValue(
    @RequestHeader(ApiParams.corrId) corrId: String,
    @InputArgument data: UpdateFeatureValueInput
  ): Boolean =
    withContext(useRequestContext(currentCoroutineContext())) {
      log.debug("[$corrId] updateFeature $data")
      featureService.updateFeatureValue(corrId, UUID.fromString(data.id), data.value.numVal, data.value.boolVal)
      true
    }
}
