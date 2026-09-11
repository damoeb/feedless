package org.migor.feedless.data.jpa.user

import kotlin.test.Test
import kotlin.test.assertNull

class UserMapperTest {

  @Test
  fun `maps a user without last login`() {
    val entity = UserEntity().apply {
      email = "legacy@example.org"
      lastLogin = null
    }

    val user = entity.toDomain()

    assertNull(user.lastLogin)
  }
}
