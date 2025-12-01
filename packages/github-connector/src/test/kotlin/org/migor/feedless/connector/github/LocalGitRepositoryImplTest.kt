package org.migor.feedless.connector.github

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.migor.feedless.connector.git.GitConnectionCapability
import org.migor.feedless.util.JsonSerializer.fromJson
import org.migor.feedless.util.JsonSerializer.toJson
import java.io.File

class LocalGitRepositoryImplTest {

  private val repositoriesToCleanup = mutableListOf<LocalGitRepositoryImpl>()

  @AfterEach
  fun cleanup() {
    repositoriesToCleanup.forEach { repo ->
      try {
        repo.close()
        val capability = repo.capability()
        capability.capabilityPayload.directory.deleteRecursively()
      } catch (_: Exception) {
        // ignore
      }
    }
    repositoriesToCleanup.clear()
  }

  @Test
  fun `test repository structure with repo subfolder and metadata at container level`() {
    val repoUrl = "https://github.com/octocat/Hello-World.git"
    val githubCapability = GithubCapability("")
    val accountConfig = GithubAccountConfig(githubCapability)
    val gitConnectionCapability = GitConnectionCapability(accountConfig)

    val repo = LocalGitRepositoryImpl.clone(
      repoUrl = repoUrl,
      token = null,
      gitConnectionCapability = gitConnectionCapability
    ) as LocalGitRepositoryImpl

    repositoriesToCleanup.add(repo)

    val capability = repo.capability()
    val containerDir = capability.capabilityPayload.directory

    assertThat(containerDir).exists()
    assertThat(containerDir.isDirectory).isTrue()

    val repoDir = File(containerDir, "repo")
    assertThat(repoDir).exists()
    assertThat(repoDir.isDirectory).isTrue()


    val metadataFile = File(containerDir, "stat.json")
    assertThat(metadataFile).exists()
    assertThat(metadataFile.isFile).isTrue()
  }

  @Test
  fun `test clone with checkout branch`() {
    val repoUrl = "https://github.com/octocat/Hello-World.git"
    val githubCapability = GithubCapability("")
    val accountConfig = GithubAccountConfig(githubCapability)
    val gitConnectionCapability = GitConnectionCapability(accountConfig)

    val repo = LocalGitRepositoryImpl.clone(
      repoUrl = repoUrl,
      token = null,
      gitConnectionCapability = gitConnectionCapability
    ) as LocalGitRepositoryImpl

    repositoriesToCleanup.add(repo)

    val checkedOutRepo = repo.checkout("master")
    assertThat(checkedOutRepo).isNotNull

    val files = checkedOutRepo.files()
    assertThat(files).isNotEmpty
  }

  @Test
  fun `test metadata is created on clone`() {
    val repoUrl = "https://github.com/octocat/Hello-World.git"
    val githubCapability = GithubCapability("")
    val accountConfig = GithubAccountConfig(githubCapability)
    val gitConnectionCapability = GitConnectionCapability(accountConfig)

    // Clone the repository
    val repo = LocalGitRepositoryImpl.clone(
      repoUrl = repoUrl,
      token = null,
      gitConnectionCapability = gitConnectionCapability
    ) as LocalGitRepositoryImpl

    repositoriesToCleanup.add(repo)

    val metadata = repo.getMetadata()

    assertThat(metadata).isNotNull
    assertThat(metadata!!.createdAt).isNotNull()
    assertThat(metadata.lastUsed).isNotNull()
    assertThat(metadata.createdAt).isEqualTo(metadata.lastUsed) // Should be equal on creation
  }

  @Test
  fun `round-trip serialization of StatData should preserve all data`() {
    // given
    val original = StatData(
      createdAt = "2024-12-01T10:30:00Z",
      lastUsed = "2024-12-01T12:45:30Z"
    )

    // when
    val jsonString = toJson(original)
    val deserialized = fromJson<StatData>(jsonString)

    // then
    assertThat(deserialized).isEqualTo(original)
    assertThat(deserialized.createdAt).isEqualTo(original.createdAt)
    assertThat(deserialized.lastUsed).isEqualTo(original.lastUsed)
  }
}

