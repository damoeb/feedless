package org.migor.feedless.connector.github

import kotlinx.serialization.json.Json
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import org.migor.feedless.capability.Capability
import org.migor.feedless.capability.CapabilityId
import org.migor.feedless.connector.git.GitConnectionCapability
import org.migor.feedless.connector.git.LocalGitRepository
import org.migor.feedless.connector.git.LocalGitRepositoryCapability
import org.migor.feedless.connector.git.LocalGitRepositoryFile
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermission
import java.nio.file.attribute.PosixFilePermissions
import java.time.Instant

data class StatData(
  val createdAt: String,
  var lastUsed: String,
)

class LocalGitRepositoryImpl private constructor(
  private val containerDir: File,  // Parent directory containing metadata and repo subfolder
  private val git: Git,
  private val gitConnectionCapability: GitConnectionCapability
) : LocalGitRepository {

  private val repoDir: File = File(containerDir, REPO_SUBFOLDER)

  companion object {
    private const val STAT_FILENAME = "stat.json"
    private const val REPO_SUBFOLDER = "repo"

    fun clone(
      repoUrl: String,
      token: String? = null,
      gitConnectionCapability: GitConnectionCapability
    ): LocalGitRepository {
      val dirPermissions = PosixFilePermissions.asFileAttribute(
        setOf(
          PosixFilePermission.OWNER_READ,
          PosixFilePermission.OWNER_WRITE,
          PosixFilePermission.OWNER_EXECUTE
        )
      )
      val containerDir = Files.createTempDirectory("git-repo-", dirPermissions).toFile()

      val repoDir = File(containerDir, REPO_SUBFOLDER)
      repoDir.mkdirs()

      val cloneCommand = Git.cloneRepository()
        .setURI(repoUrl)
        .setDirectory(repoDir)

      if (token != null) {
        val credentials = UsernamePasswordCredentialsProvider("oauth2", token)
        cloneCommand.setCredentialsProvider(credentials)
      }

      cloneCommand.setDepth(10)

      val git = cloneCommand.call()

      val instance = LocalGitRepositoryImpl(containerDir, git, gitConnectionCapability)
      instance.createStatFile()

      return instance
    }
  }

  override fun checkout(branch: String): LocalGitRepository {
    updateLastUsedTimestamp()
    git.checkout().setName(branch).call()
    return this
  }

  override fun files(): List<LocalGitRepositoryFile> {
    updateLastUsedTimestamp()
    val filesList = mutableListOf<LocalGitRepositoryFile>()

    repoDir.walkTopDown()
      .filter { it.isFile && !it.path.contains("/.git/") }
      .forEach { file ->
        filesList.add(LocalGitRepositoryFile())
      }

    return filesList
  }

  override fun add(files: List<LocalGitRepositoryFile>): LocalGitRepository {
    updateLastUsedTimestamp()
    git.add().addFilepattern(".").call()
    return this
  }

  override fun commit(): LocalGitRepository {
    updateLastUsedTimestamp()
    git.commit().setMessage("Automated commit").call()
    return this
  }

  override fun push(): LocalGitRepository {
    updateLastUsedTimestamp()
    git.push().call()
    return this
  }

  override fun capability(): Capability<LocalGitRepositoryCapability> {
    return Capability(CapabilityId("repository"), LocalGitRepositoryCapability(containerDir, gitConnectionCapability))
  }

  fun close() {
    git.close()
  }

  private fun createStatFile() {
    val now = Instant.now().toString()
    val metadata = StatData(
      createdAt = now,
      lastUsed = now,
    )
    writeStatFile(metadata)
  }

  private fun updateLastUsedTimestamp() {
    val metadata = readStatData()
    if (metadata != null) {
      metadata.lastUsed = Instant.now().toString()
      writeStatFile(metadata)
    }
  }

  private fun readStatData(): StatData? {
    val statFile = File(containerDir, STAT_FILENAME)
    return if (statFile.exists()) {
      try {
        Json.decodeFromString<StatData>(statFile.readText())
      } catch (_: Exception) {
        null
      }
    } else {
      null
    }
  }

  private fun writeStatFile(statData: StatData) {
    val metadataFile = File(containerDir, STAT_FILENAME)
    metadataFile.writeText(Json.encodeToString(statData))
  }

  fun getMetadata(): StatData? {
    return readStatData()
  }
}
