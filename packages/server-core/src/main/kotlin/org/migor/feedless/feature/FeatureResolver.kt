package org.migor.feedless.feature

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsMutation
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import graphql.schema.DataFetchingEnvironment
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.throttle.Throttled
import org.migor.feedless.generated.DgsConstants
import org.migor.feedless.generated.types.FeatureGroup
import org.migor.feedless.generated.types.FeatureGroupWhereInput
import org.migor.feedless.generated.types.UpdateFeatureValueInput
import org.migor.feedless.session.injectCurrentUser
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@DgsComponent
@Profile("${AppProfiles.features} & ${AppLayer.api}")
class FeatureResolver(
  private val featureService: FeatureService
) {

  private val log = LoggerFactory.getLogger(FeatureResolver::class.simpleName)

//  @Autowired
//  private lateinit var sessionService: SessionService

  @Throttled
  @DgsQuery
  @Transactional
  suspend fun featureGroups(
    dfe: DataFetchingEnvironment,
    @InputArgument inherit: Boolean,
    @InputArgument where: FeatureGroupWhereInput,
  ): List<FeatureGroup> =
    withContext(injectCurrentUser(currentCoroutineContext(), dfe)) {
      log.debug("featureGroups inherit=$inherit where=$where")
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
    dfe: DataFetchingEnvironment,
    @InputArgument data: UpdateFeatureValueInput
  ): Boolean =
    withContext(injectCurrentUser(currentCoroutineContext(), dfe)) {
      log.debug("updateFeature $data")
      featureService.updateFeatureValue(UUID.fromString(data.id), data.value.numVal, data.value.boolVal)
      true
    }
}
