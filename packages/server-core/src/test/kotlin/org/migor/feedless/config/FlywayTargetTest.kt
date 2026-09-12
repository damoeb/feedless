package org.migor.feedless.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
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

  // Rebasing onto develop can leave two branches' V<n> side by side; Flyway would refuse to start.
  @Test
  fun `no two migrations share a version`() {
    val duplicates = migrationsByVersion().filterValues { it.size > 1 }

    assertTrue(
      duplicates.isEmpty(),
      "duplicate migration versions — renumber the unreleased ones: " +
        duplicates.entries.joinToString { (version, files) -> "V$version: ${files.sorted()}" }
    )
  }

  private fun highestMigrationVersion(): Int = migrationsByVersion().keys.max()

  private fun migrationsByVersion(): Map<Int, List<String>> {
    val migrationDir = migrationDirectory()
    val migrations = migrationDir.listFiles()
      .orEmpty()
      .mapNotNull { file -> migrationFilePattern.find(file.name)?.let { it.groupValues[1].toInt() to file.name } }

    check(migrations.isNotEmpty()) { "found no V<n>__*.sql migrations under ${migrationDir.path}" }
    return migrations.groupBy({ it.first }, { it.second })
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
