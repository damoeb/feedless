package org.migor.feedless.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

/** Flyway stops at spring.flyway.target, so a migration added without raising it is never applied. */
class FlywayTargetTest {

  private val migrationFilePattern = Regex("""^V(\d+)__.*\.sql$""")

  companion object {
    private const val PINNED_OUT_MARKER = "-- flyway:pinned-out"
  }
  private val flywayTargetPattern = Regex("""(?m)^\s*target:\s*(\d+)\s*$""")

  @Test
  fun `spring flyway target matches the highest shipped migration version`() {
    val highestMigrationVersion = highestAppliedMigrationVersion()
    val configuredTarget = configuredFlywayTarget()

    assertEquals(
      highestMigrationVersion,
      configuredTarget,
      "new migration V$highestMigrationVersion added but spring.flyway.target is $configuredTarget — raise the target"
    )
  }

  @Test
  fun `pinned-out migrations all lie above the target`() {
    val target = configuredFlywayTarget()
    val misplaced = pinnedOutVersions().filter { it <= target }

    assertTrue(misplaced.isEmpty(), "pinned-out migrations at or below target $target would be applied: V$misplaced")
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

  // Staged destructive migrations ship pinned out, so the target is raised deliberately later.
  private fun pinnedOutVersions(): Set<Int> = migrationDirectory().listFiles().orEmpty()
    .filter { it.readLines().firstOrNull()?.trim() == PINNED_OUT_MARKER }
    .mapNotNull { migrationFilePattern.find(it.name)?.groupValues?.get(1)?.toInt() }
    .toSet()

  private fun highestAppliedMigrationVersion(): Int = (migrationsByVersion().keys - pinnedOutVersions()).max()

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

  private fun migrationDirectory() = resolveFile(
    "../jpa-data/src/main/resources/db/migration",
    "packages/jpa-data/src/main/resources/db/migration"
  )

  private fun applicationDatabaseYaml() = resolveFile(
    "src/main/resources/application-database.yaml",
    "packages/server-core/src/main/resources/application-database.yaml"
  )

  /** Works from the module or the repository root (IDE run configurations). */
  private fun resolveFile(moduleRelative: String, repoRelative: String): File {
    val fromModuleRoot = File(moduleRelative)
    if (fromModuleRoot.exists()) {
      return fromModuleRoot
    }

    val fromRepoRoot = File(repoRelative)
    if (fromRepoRoot.exists()) {
      return fromRepoRoot
    }

    error(
      "could not locate $moduleRelative relative to module root (${File(".").canonicalPath}) " +
        "nor $repoRelative relative to the repository root"
    )
  }
}
