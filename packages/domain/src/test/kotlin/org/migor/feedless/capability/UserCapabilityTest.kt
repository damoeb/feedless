package org.migor.feedless.capability

import org.migor.feedless.user.UserId
import org.migor.feedless.util.JsonSerializer.toJson
import kotlin.test.Test
import kotlin.test.assertEquals

class UserCapabilityTest {

  @Test
  fun json() {
    val userId = UserId()
    val userCapability = UserCapability(userId)

    val jsonString = toJson(userCapability)
    val capability = UnresolvedCapability(UserCapability.ID, jsonString)

    val actual = UserCapability.resolve(capability)

    assertEquals(userId.uuid, actual.userId.uuid)
    assertEquals(UserCapability.ID.value, actual.capabilityId.value)
  }

}
