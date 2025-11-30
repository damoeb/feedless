package org.migor.feedless.connector.github

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.migor.feedless.PageableRequest
import org.migor.feedless.capability.UnresolvedCapability
import org.migor.feedless.util.JsonSerializer.toJson
import kotlin.test.Test

class GitRepositoryProviderTest {
  @Test
  @Disabled
  fun query() = runTest {
    val git = GitRepositoryProvider()
    val pageable = PageableRequest(0, 10)
    val githubCapability = GithubCapability("xx")
    val capability = UnresolvedCapability(GithubCapability.ID, toJson(githubCapability))
    val repositories = git.provideAll(capability, pageable, null)

    assertThat(repositories).isNotNull()
  }
}
