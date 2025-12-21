package org.migor.feedless.feature

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsMutation
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import graphql.schema.DataFetchingEnvironment
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.throttle.Throttled
import org.migor.feedless.generated.DgsConstants
import org.migor.feedless.generated.types.FeatureGroupWhereInput
import org.migor.feedless.generated.types.UpdateFeatureValueInput
import org.migor.feedless.session.createRequestContext
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.migor.feedless.generated.types.FeatureGroup as FeatureGroupDto

@DgsComponent
@Profile("${AppProfiles.features} & ${AppLayer.api}")
class FeatureResolver(
  private val featureService: FeatureService
) {

  private val log = LoggerFactory.getLogger(FeatureResolver::class.simpleName)

//  @Autowired
//  private lateinit var sessionService: SessionService

  @Throttled
  @DgsQuery(field = DgsConstants.QUERY.FeatureGroups)
  suspend fun featureGroups(
    dfe: DataFetchingEnvironment,
    @InputArgument(DgsConstants.QUERY.FEATUREGROUPS_INPUT_ARGUMENT.Inherit) inherit: Boolean,
    @InputArgument(DgsConstants.QUERY.FEATUREGROUPS_INPUT_ARGUMENT.Where) where: FeatureGroupWhereInput,
  ): List<FeatureGroupDto> = withContext(context = createRequestContext()) {
    log.debug("featureGroups inherit=$inherit where=$where")
    featureService.findAllGroups(inherit, where).map { it.toDto() }
  }

//  @DgsData(parentType = DgsConstants.FEATUREGROUP.TYPE_NAME)
//  suspend fun features(dfe: DgsDataFetchingEnvironment): List<Feature> = coroutineScope {
//    val group: FeatureGroup = dfe.getSourceOrThrow()
//    featureService.findAllByGroupId(UUID.fromString(group.id))
//  }

  @Throttled
  @DgsMutation(field = DgsConstants.MUTATION.UpdateFeatureValue)
  suspend fun updateFeatureValue(
    dfe: DataFetchingEnvironment,
    @InputArgument(DgsConstants.MUTATION.UPDATEFEATUREVALUE_INPUT_ARGUMENT.Data) data: UpdateFeatureValueInput
  ): Boolean = withContext(context = createRequestContext()) {
    log.debug("updateFeature $data")
    featureService.updateFeatureValue(FeatureValueId(data.id), data.value.numVal, data.value.boolVal)
    true
  }
}


private fun FeatureGroup.toDto(): FeatureGroupDto {
  return FeatureGroupDto(
    id = id.uuid.toString(),
    name = name,
    parentId = parentFeatureGroupId?.uuid?.toString(),
    features = emptyList() // todo resolve features
  )
}
