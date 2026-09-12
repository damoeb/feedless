package org.migor.feedless.feature

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsMutation
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import graphql.schema.DataFetchingEnvironment
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.throttle.Throttled
import org.migor.feedless.generated.DgsConstants
import org.migor.feedless.generated.types.FeatureGroupWhereInput
import org.migor.feedless.generated.types.UpdateFeatureValueInput
import org.migor.feedless.session.injectCapabilitiesFromSecurityContext
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.migor.feedless.generated.types.FeatureGroup as FeatureGroupDto
import org.migor.feedless.generated.types.FeatureName as FeatureNameDto

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
  ): List<FeatureGroupDto> = withContext(context = injectCapabilitiesFromSecurityContext()) {
    log.debug("featureGroups inherit=$inherit where=$where")
    featureService.findAllGroups(inherit, where.id?.let { FeatureGroupId(it.eq!!) }).map { it.toDto() }
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
  ): Boolean = withContext(context = injectCapabilitiesFromSecurityContext()) {
    log.debug("updateFeature $data")
    featureService.updateFeatureValue(FeatureValueId(data.id), data.value.numVal?.value, data.value.boolVal?.value)
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

val mapFeatureName2Dto = mapOf(
  FeatureName.canJoinPlanWaitList to FeatureNameDto.canJoinPlanWaitList,
  FeatureName.canActivatePlan to FeatureNameDto.canActivatePlan,

  FeatureName.requestPerMinuteUpperLimitInt to FeatureNameDto.requestPerMinuteUpperLimitInt,
  FeatureName.refreshRateInMinutesLowerLimitInt to FeatureNameDto.refreshRateInMinutesLowerLimit,
  FeatureName.publicRepositoryBool to FeatureNameDto.publicRepository,

  FeatureName.scrapeRequestTimeoutMsecInt to FeatureNameDto.scrapeRequestTimeoutMsec,
  FeatureName.repositoryRetentionMaxDaysLowerLimitInt to FeatureNameDto.repositoryRetentionMaxDaysLowerLimitInt,
  FeatureName.repositoryCapacityUpperLimitInt to FeatureNameDto.repositoryCapacityUpperLimitInt,
  FeatureName.repositoriesMaxCountTotalInt to FeatureNameDto.repositoriesMaxCountTotalInt,
  FeatureName.sourceMaxCountPerRepositoryInt to FeatureNameDto.sourceMaxCountPerRepositoryInt,

  FeatureName.pluginsBool to FeatureNameDto.plugins,
//  FeatureName.repositoriesMaxCountActiveInt to FeatureNameDto.scrapeSourceMaxCountActive,
//  FeatureName.repositoriesMaxCountTotalInt to FeatureNameDto.scrapeSourceMaxCountTotal,
//  FeatureName.sourceMaxCountPerRepositoryInt to FeatureNameDto.scrapeRequestMaxCountPerSource,
)

//private fun FeatureGroup.toDto(): FeatureGroupDto? {
//  return try {
////    val featureName = FeatureName.valueOf(name)
////    mapFeatureName2Dto[featureName]
//  } catch (e: Exception) {
//    null
//  }
//}
