package org.migor.feedless.config

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

class FlywayMigrationsTest {

  private val migrationFilePattern = Regex("""^V(\d+)__.*\.sql$""")

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

  private fun migrationsByVersion(): Map<Int, List<String>> {
    val migrationDir = migrationDirectory()
    val migrations = migrationDir.listFiles()
      .orEmpty()
      .mapNotNull { file -> migrationFilePattern.find(file.name)?.let { it.groupValues[1].toInt() to file.name } }

    check(migrations.isNotEmpty()) { "found no V<n>__*.sql migrations under ${migrationDir.path}" }
    return migrations.groupBy({ it.first }, { it.second })
  }

  /** Works from the module or the repository root (IDE run configurations). */
  private fun migrationDirectory(): File {
    val moduleRelative = "../jpa-data/src/main/resources/db/migration"
    val repoRelative = "packages/jpa-data/src/main/resources/db/migration"
    return listOf(File(moduleRelative), File(repoRelative)).firstOrNull { it.exists() }
      ?: error(
        "could not locate $moduleRelative relative to module root (${File(".").canonicalPath}) " +
          "nor $repoRelative relative to the repository root"
      )
  }
}
