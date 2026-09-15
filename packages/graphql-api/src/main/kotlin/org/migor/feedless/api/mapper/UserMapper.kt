package org.migor.feedless.api.mapper

import org.apache.commons.lang3.StringUtils
import org.migor.feedless.user.User
import org.migor.feedless.util.toMillis
import org.migor.feedless.generated.types.User as UserDto

fun User.toDto(): UserDto = UserDto(
    id = id.uuid.toString(),
    firstName = firstName,
    lastName = lastName,
    country = country,
    notificationRepositoryId = inboxRepositoryId?.uuid.toString(),
    notificationsLastViewedAt = notificationsLastViewedAt?.toMillis(),
    secrets = emptyList(),
    connectedApps = emptyList(),
    groups = emptyList(),
    features = emptyList(),
    emailValidated = hasValidatedEmail,
    createdAt = createdAt.toMillis(),
    email = email,
    hasCompletedSignup = StringUtils.isNotBlank(email),
    purgeScheduledFor = purgeScheduledFor?.toMillis(),
    hasAcceptedTerms = hasAcceptedTerms,
)
