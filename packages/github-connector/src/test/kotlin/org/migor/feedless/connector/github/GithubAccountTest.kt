package org.migor.feedless.connector.github

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.migor.feedless.util.JsonSerializer.fromJson
import org.migor.feedless.util.JsonSerializer.toJson

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

  @Test
  fun `round-trip serialization of GithubRepository should preserve all data`() {
    // given
    val original = GithubRepository(
      id = 123456,
      name = "test-repo",
      fullName = "octocat/test-repo",
      html_url = "https://github.com/octocat/test-repo",
      url = "https://api.github.com/repos/octocat/test-repo",
      updated_at = "2024-12-01T10:30:00Z",
      size = 1024,
      archived = false,
      disabled = false,
      visibility = "public"
    )

    // when
    val jsonString = toJson(original)
    val deserialized = fromJson<GithubRepository>(jsonString)

    // then
    assertThat(deserialized).isEqualTo(original)
    assertThat(deserialized.id).isEqualTo(original.id)
    assertThat(deserialized.name).isEqualTo(original.name)
    assertThat(deserialized.fullName).isEqualTo(original.fullName)
    assertThat(deserialized.html_url).isEqualTo(original.html_url)
    assertThat(deserialized.url).isEqualTo(original.url)
    assertThat(deserialized.updated_at).isEqualTo(original.updated_at)
    assertThat(deserialized.size).isEqualTo(original.size)
    assertThat(deserialized.archived).isEqualTo(original.archived)
    assertThat(deserialized.disabled).isEqualTo(original.disabled)
    assertThat(deserialized.visibility).isEqualTo(original.visibility)
  }

  @Test
  fun `round-trip serialization of List of GithubRepository should preserve all data`() {
    // given
    val original = listOf(
      GithubRepository(
        id = 123,
        name = "repo1",
        fullName = "user/repo1",
        html_url = "https://github.com/user/repo1",
        url = "https://api.github.com/repos/user/repo1",
        updated_at = "2024-12-01T10:00:00Z",
        size = 512,
        archived = false,
        disabled = false,
        visibility = "public"
      ),
      GithubRepository(
        id = 456,
        name = "repo2",
        fullName = "user/repo2",
        html_url = "https://github.com/user/repo2",
        url = "https://api.github.com/repos/user/repo2",
        updated_at = "2024-12-01T11:00:00Z",
        size = 2048,
        archived = true,
        disabled = false,
        visibility = "private"
      )
    )

    // when
    val jsonString = toJson(original)
    val deserialized = fromJson<List<GithubRepository>>(jsonString)

    // then
    assertThat(deserialized).hasSize(original.size)
    assertThat(deserialized).isEqualTo(original)

    for (i in original.indices) {
      assertThat(deserialized[i].id).isEqualTo(original[i].id)
      assertThat(deserialized[i].name).isEqualTo(original[i].name)
      assertThat(deserialized[i].fullName).isEqualTo(original[i].fullName)
    }
  }
}
