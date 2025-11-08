package org.migor.feedless.connector.github

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class GithubAccountTest {

  @Test
  @Disabled("Not implemented yet")
  fun repositories() {
    val accountConfig =
      GithubAccountConfig(GithubCapability(""))
    val account = GithubAccount(accountConfig)

    val repositories = account.repositories()
    assertThat(repositories).isNotNull()
  }
}
