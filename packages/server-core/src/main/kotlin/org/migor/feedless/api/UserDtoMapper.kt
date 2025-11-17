package org.migor.feedless.api

import org.apache.commons.lang3.StringUtils
import org.migor.feedless.data.jpa.user.UserEntity
import org.migor.feedless.generated.types.User
import org.migor.feedless.util.toMillis


fun UserEntity.toDTO(): User =
  User(
    id = id.toString(),
    createdAt = createdAt.toMillis(),
    purgeScheduledFor = purgeScheduledFor?.toMillis(),
    hasAcceptedTerms = hasAcceptedTerms,
    hasCompletedSignup = hasFinalizedProfile(),
    email = StringUtils.trimToEmpty(email),
    emailValidated = hasValidatedEmail,
    firstName = StringUtils.trimToEmpty(firstName),
    lastName = StringUtils.trimToEmpty(lastName),
    country = StringUtils.trimToEmpty(country),
    notificationRepositoryId = inboxRepositoryId?.toString(),
    secrets = emptyList(),
    connectedApps = emptyList(),
    groups = emptyList(),
    features = emptyList()
//          .dateFormat(propertyService.dateFormat)
//          .timeFormat(propertyService.timeFormat)
  )

