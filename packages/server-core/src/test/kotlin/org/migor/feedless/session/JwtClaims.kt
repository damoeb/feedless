package org.migor.feedless.session

import org.migor.feedless.capability.GroupCapability
import org.migor.feedless.capability.UserCapability
import org.migor.feedless.group.GroupAndRole
import org.migor.feedless.user.UserId
import org.springframework.security.oauth2.jwt.Jwt

/** The group capability a token carries, read the way request handling reads it. */
fun Jwt.actingGroupClaim(): GroupAndRole? =
  capabilities().singleOrNull { it.authority == GroupCapability.ID.value }
    ?.let { GroupCapability.fromString(it.payload) }

/** The user capability a token carries, read the way request handling reads it. */
fun Jwt.userClaim(): UserId? =
  capabilities().singleOrNull { it.authority == UserCapability.ID.value }
    ?.let { UserCapability.fromString(it.payload) }
