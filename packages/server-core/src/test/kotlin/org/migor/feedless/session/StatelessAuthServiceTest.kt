package org.migor.feedless.session

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.user.User
import org.migor.feedless.user.UserId
import org.springframework.test.util.ReflectionTestUtils

class StatelessAuthServiceTest {

  private val service = StatelessAuthService()
  private lateinit var root: User

  @BeforeEach
  fun setUp() {
    ReflectionTestUtils.setField(service, "rootEmail", "root@example.org")
    ReflectionTestUtils.setField(service, "rootSecretKey", "root-secret")
    service.init()
    root = ReflectionTestUtils.getField(service, "root") as User
  }

  @Test
  fun `findUserById finds the root user by its id`() = runTest {
    assertThat(service.findUserById(root.id)).isEqualTo(root)
  }

  @Test
  fun `findUserById finds nobody else`() = runTest {
    assertThat(service.findUserById(UserId())).isNull()
  }
}
