package org.migor.feedless.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

/** Flyway stops at spring.flyway.target, so a migration added without raising it is never applied. */
class FlywayTargetTest {

  private val migrationFilePattern = Regex("""^V(\d+)__.*\.sql$""")
  private val flywayTargetPattern = Regex("""(?m)^\s*target:\s*(\d+)\s*$""")

  @Test
  fun `spring flyway target matches the highest shipped migration version`() {
    val highestMigrationVersion = highestMigrationVersion()
    val configuredTarget = configuredFlywayTarget()

    assertEquals(
      highestMigrationVersion,
      configuredTarget,
      "new migration V$highestMigrationVersion added but spring.flyway.target is $configuredTarget — raise the target"
    )
  }

  private fun highestMigrationVersion(): Int {
    val migrationDir = migrationDirectory()
    val versions = migrationDir.listFiles()
      .orEmpty()
      .mapNotNull { migrationFilePattern.find(it.name)?.groupValues?.get(1)?.toInt() }

    check(versions.isNotEmpty()) { "found no V<n>__*.sql migrations under ${migrationDir.path}" }
    return versions.max()
  }

  private fun configuredFlywayTarget(): Int {
    val yamlFile = applicationDatabaseYaml()
    val match = flywayTargetPattern.find(yamlFile.readText())
      ?: error("could not find 'spring.flyway.target' in ${yamlFile.path}")
    return match.groupValues[1].toInt()
  }

  private fun migrationDirectory() = resolveServerCoreFile("src/main/resources/db/migration")

  private fun applicationDatabaseYaml() = resolveServerCoreFile("src/main/resources/application-database.yaml")

  /** Works from the module or the repository root (IDE run configurations). */
  private fun resolveServerCoreFile(relativePath: String): File {
    val fromModuleRoot = File(relativePath)
    if (fromModuleRoot.exists()) {
      return fromModuleRoot
    }

    val fromRepoRoot = File("packages/server-core", relativePath)
    if (fromRepoRoot.exists()) {
      return fromRepoRoot
    }

    error(
      "could not locate $relativePath relative to module root (${File(".").canonicalPath}); " +
        "tried both the module directory and packages/server-core/ under the current directory"
    )
  }
}
