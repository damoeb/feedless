package org.migor.feedless.connector.git

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.PageableRequest
import org.migor.feedless.capability.CapabilityId
import org.migor.feedless.capability.UnresolvedCapability
import org.migor.feedless.document.DatesWhereInput
import org.migor.feedless.document.DocumentsFilter
import org.migor.feedless.document.RecordOrderBy
import org.migor.feedless.document.ReleaseStatus
import org.migor.feedless.document.StringFilter
import org.migor.feedless.repository.RepositoryId
import java.io.File
import java.nio.file.Files
import java.time.LocalDateTime

class GitDocumentProviderTest {

  private lateinit var tempDir: File
  private lateinit var repoDir: File
  private lateinit var provider: GitDocumentProvider
  private lateinit var repositoryId: RepositoryId

  @BeforeEach
  fun setup() {
    // Create temporary directory structure
    tempDir = Files.createTempDirectory("git-test-").toFile()
    repoDir = File(tempDir, "repo")
    repoDir.mkdirs()

    provider = GitDocumentProvider()
    repositoryId = RepositoryId()

    // Create test files
    createTestFiles()
  }

  @AfterEach
  fun cleanup() {
    tempDir.deleteRecursively()
  }

  private fun createTestFiles() {
    // Create some test files with different extensions
    File(repoDir, "readme.md").writeText("# Test Repository\n\nThis is a test.")
    File(repoDir, "index.html").writeText("<html><body>Hello World</body></html>")
    File(repoDir, "script.js").writeText("console.log('test');")

    // Create subdirectory with files
    val subDir = File(repoDir, "src")
    subDir.mkdirs()
    File(subDir, "main.kt").writeText("fun main() { println(\"Hello\") }")
    File(subDir, "utils.kt").writeText("object Utils { }")
  }

  private fun createCapability(): UnresolvedCapability {
    // Create JSON payload manually since File is not directly serializable
    val payload = """{"directory":"${tempDir.absolutePath}"}"""
    return UnresolvedCapability(
      capabilityId = GitDocumentProvider.CAPABILITY_ID,
      capabilityPayload = payload
    )
  }

  @Test
  fun `should recognize expected capability`() = runTest {
    assertTrue(provider.expectsCapabilities(GitDocumentProvider.CAPABILITY_ID))
    assertFalse(provider.expectsCapabilities(CapabilityId("other-capability")))
  }

  @Test
  fun `should provide all documents from git repository`() = runTest {
    val capability = createCapability()
    val filter = DocumentsFilter(repository = repositoryId)
    val pageable = PageableRequest(pageNumber = 0, pageSize = 100)
    val order = RecordOrderBy()

    val documents = provider.provideAll(capability, pageable, filter, order)

    assertEquals(5, documents.size, "Should find all 5 test files")

    // Verify document properties
    documents.forEach { doc ->
      assertNotNull(doc.url)
      assertNotNull(doc.contentHash)
      assertTrue(doc.url.startsWith("file://"))
      assertEquals(repositoryId, doc.repositoryId)
      assertEquals(ReleaseStatus.released, doc.status)
    }
  }

  @Test
  fun `should handle pagination correctly`() = runTest {
    val capability = createCapability()
    val filter = DocumentsFilter(repository = repositoryId)
    val order = RecordOrderBy()

    // First page
    val page1 = provider.provideAll(
      capability,
      PageableRequest(pageNumber = 0, pageSize = 2),
      filter,
      order
    )
    assertEquals(2, page1.size)

    // Second page
    val page2 = provider.provideAll(
      capability,
      PageableRequest(pageNumber = 1, pageSize = 2),
      filter,
      order
    )
    assertEquals(2, page2.size)

    // Third page
    val page3 = provider.provideAll(
      capability,
      PageableRequest(pageNumber = 2, pageSize = 2),
      filter,
      order
    )
    assertEquals(1, page3.size)

    // Verify no overlap
    val allIds = (page1 + page2 + page3).map { it.id }.toSet()
    assertEquals(5, allIds.size, "All documents should have unique IDs")
  }

  @Test
  fun `should filter documents by tags`() = runTest {
    val capability = createCapability()
    val filter = DocumentsFilter(
      repository = repositoryId,
      tags = StringFilter(eq = "md")
    )
    val pageable = PageableRequest(pageNumber = 0, pageSize = 100)
    val order = RecordOrderBy()

    val documents = provider.provideAll(capability, pageable, filter, order)

    assertTrue(documents.size >= 1, "Should find at least one .md file")
    documents.forEach { doc ->
      assertTrue(doc.tags?.contains("md") == true, "Document should have 'md' tag")
    }
  }

  @Test
  fun `should extract text content from text files`() = runTest {
    val capability = createCapability()
    val filter = DocumentsFilter(repository = repositoryId)
    val pageable = PageableRequest(pageNumber = 0, pageSize = 100)
    val order = RecordOrderBy()

    val documents = provider.provideAll(capability, pageable, filter, order)

    val readmeDoc = documents.find { it.title == "readme.md" }
    assertNotNull(readmeDoc)
    assertTrue(readmeDoc!!.text.contains("Test Repository"))
  }

  @Test
  fun `should identify HTML files correctly`() = runTest {
    val capability = createCapability()
    val filter = DocumentsFilter(repository = repositoryId)
    val pageable = PageableRequest(pageNumber = 0, pageSize = 100)
    val order = RecordOrderBy()

    val documents = provider.provideAll(capability, pageable, filter, order)

    val htmlDoc = documents.find { it.title == "index.html" }
    assertNotNull(htmlDoc)
    assertNotNull(htmlDoc!!.html, "HTML document should have html content")
    assertTrue(htmlDoc.html!!.contains("<html>"))
  }

  @Test
  fun `should handle non-existent repository gracefully`() = runTest {
    val payload = """{"directory":"/non/existent/path"}"""
    val capability = UnresolvedCapability(
      capabilityId = GitDocumentProvider.CAPABILITY_ID,
      capabilityPayload = payload
    )

    val filter = DocumentsFilter(repository = repositoryId)
    val pageable = PageableRequest(pageNumber = 0, pageSize = 100)
    val order = RecordOrderBy()

    val documents = provider.provideAll(capability, pageable, filter, order)

    assertEquals(0, documents.size, "Should return empty list for non-existent repository")
  }

  @Test
  fun `should filter by date range`() = runTest {
    val capability = createCapability()
    val now = LocalDateTime.now()
    val filter = DocumentsFilter(
      repository = repositoryId,
      createdAt = DatesWhereInput(
        after = now.minusDays(1),
        before = now.plusDays(1)
      )
    )
    val pageable = PageableRequest(pageNumber = 0, pageSize = 100)
    val order = RecordOrderBy()

    val documents = provider.provideAll(capability, pageable, filter, order)

    assertEquals(5, documents.size, "All files created today should be included")
    documents.forEach { doc ->
      assertTrue(doc.createdAt.isAfter(now.minusDays(1)))
      assertTrue(doc.createdAt.isBefore(now.plusDays(1)))
    }
  }

  @Test
  fun `should generate unique content hashes`() = runTest {
    val capability = createCapability()
    val filter = DocumentsFilter(repository = repositoryId)
    val pageable = PageableRequest(pageNumber = 0, pageSize = 100)
    val order = RecordOrderBy()

    val documents = provider.provideAll(capability, pageable, filter, order)

    val hashes = documents.map { it.contentHash }.toSet()
    assertEquals(documents.size, hashes.size, "All documents should have unique content hashes")
  }

  @Test
  fun `should include file path in tags`() = runTest {
    val capability = createCapability()
    val filter = DocumentsFilter(repository = repositoryId)
    val pageable = PageableRequest(pageNumber = 0, pageSize = 100)
    val order = RecordOrderBy()

    val documents = provider.provideAll(capability, pageable, filter, order)

    val mainKtDoc = documents.find { it.title == "main.kt" }
    assertNotNull(mainKtDoc)
    assertTrue(
      mainKtDoc!!.tags?.any { it.contains("src") } == true,
      "Document should include path in tags"
    )
  }
}

