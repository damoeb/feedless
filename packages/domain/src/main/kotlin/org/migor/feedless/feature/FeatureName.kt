package org.migor.feedless.feature

enum class FeatureName {
  requestPerMinuteUpperLimitInt,
  refreshRateInMinutesLowerLimitInt,
  publicRepositoryBool,
  pluginsBool,
  legacyFeedApiBool,

  repositoryCapacityUpperLimitInt,
  repositoryCapacityLowerLimitInt,
  repositoryRetentionMaxDaysLowerLimitInt,
  itemEmailForwardBool,
  itemWebhookForwardBool,
  canLogin,
  canActivatePlan,
  canCreateUser,
  canJoinPlanWaitList,
  canSignUp,

  scrapeRequestTimeoutMsecInt,
  repositoriesMaxCountActiveInt,
  sourceMaxCountPerRepositoryInt,

  @Deprecated("will be removed")
  scrapeRequestActionMaxCountInt,
  repositoriesMaxCountTotalInt,
}
