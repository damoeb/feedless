package org.migor.feedless.capability

import org.migor.feedless.user.UserId
import org.migor.feedless.util.JsonSerializer.fromJson
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

  @Test
  fun `round-trip serialization should preserve all data`() {
    // given
    val userId = UserId()
    val original = UserCapability(userId)

    // when
    // {"uuid":"d0d352c1-af66-4749-a265-764962476ad3"}
    val jsonString = toJson(original)
    val deserialized = fromJson<UserCapability>(jsonString)

    // then
    assertEquals(original.userId.uuid, deserialized.userId.uuid)
    assertEquals(original.capabilityId.value, deserialized.capabilityId.value)
    assertEquals(original.capabilityPayload.uuid, deserialized.capabilityPayload.uuid)
  }

}
