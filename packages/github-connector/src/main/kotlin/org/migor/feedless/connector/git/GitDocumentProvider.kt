package org.migor.feedless.connector.git

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.migor.feedless.PageableRequest
import org.migor.feedless.capability.CapabilityId
import org.migor.feedless.capability.UnresolvedCapability
import org.migor.feedless.document.Document
import org.migor.feedless.document.DocumentProvider
import org.migor.feedless.document.DocumentsFilter
import org.migor.feedless.document.RecordOrderBy
import org.migor.feedless.document.ReleaseStatus
import org.migor.feedless.document.SortOrder
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.security.MessageDigest
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Document provider that reads files from a local Git repository and exposes them as documents.
 *
 * This provider:
 * - Walks through all files in a git repository directory (excluding .git folder)
 * - Converts each file to a Document with appropriate metadata
 * - Supports filtering by tags, dates, and other document properties
 * - Supports ordering and pagination
 * - Extracts text content from text files and HTML content from HTML files
 * - Generates SHA-256 content hashes for deduplication
 *
 * Expected capability: LocalGitRepositoryCapability with the repository directory path
 * Capability payload format: JSON with "directory" field pointing to the git repo parent directory
 * Example: {"directory":"/path/to/repo"}
 */
class GitDocumentProvider : DocumentProvider {

  private val log = LoggerFactory.getLogger(GitDocumentProvider::class.simpleName)

  companion object {
    val CAPABILITY_ID = CapabilityId("local-git-repository")
    private const val REPO_SUBFOLDER = "repo"
  }

  override suspend fun expectsCapabilities(capabilityId: CapabilityId): Boolean {
    return capabilityId.value == CAPABILITY_ID.value
  }

  override suspend fun provideAll(
    capability: UnresolvedCapability,
    pageable: PageableRequest,
    filter: DocumentsFilter,
    order: RecordOrderBy
  ): List<Document> {
    log.debug("Providing documents from git repository")

    // Parse the capability
    val localGitCapability = parseCapability(capability)
    val repoDir = File(localGitCapability.directory, REPO_SUBFOLDER)

    if (!repoDir.exists() || !repoDir.isDirectory) {
      log.warn("Git repository directory does not exist: ${repoDir.absolutePath}")
      return emptyList()
    }

    // Collect all files from the git repository
    val allDocuments = collectFilesAsDocuments(repoDir, filter.repository)

    // Apply filters
    val filteredDocuments = applyFilters(allDocuments, filter)

    // Apply ordering
    val orderedDocuments = applyOrdering(filteredDocuments, order)

    // Apply pagination
    return applyPagination(orderedDocuments, pageable)
  }

  private fun parseCapability(capability: UnresolvedCapability): LocalGitRepositoryCapability {
    return try {
      // todo Parse JSON manually to handle File object
      val json = Json.parseToJsonElement(capability.capabilityPayload)
      val jsonObject = json.jsonObject

      val directoryPath = jsonObject["directory"]?.jsonPrimitive?.content
        ?: throw IllegalArgumentException("Missing 'directory' field in capability")

      val directory = File(directoryPath)

      // For now, create a simple GitConnectionCapability since we don't need its details
      val gitConnectionCapability = GitConnectionCapability(
        connectionConfig = object : GitConnectionConfig {}
      )

      LocalGitRepositoryCapability(directory, gitConnectionCapability)
    } catch (e: Exception) {
      log.error("Failed to parse LocalGitRepositoryCapability", e)
      throw IllegalArgumentException("Invalid capability payload: ${e.message}", e)
    }
  }

  private fun collectFilesAsDocuments(
    repoDir: File,
    repositoryId: org.migor.feedless.repository.RepositoryId
  ): List<Document> {
    val documents = mutableListOf<Document>()

    repoDir.walkTopDown()
      .filter { it.isFile && !it.path.contains("/.git/") }
      .forEach { file ->
        try {
          val document = fileToDocument(file, repoDir, repositoryId)
          documents.add(document)
        } catch (e: Exception) {
          log.warn("Failed to convert file to document: ${file.absolutePath}", e)
        }
      }

    return documents
  }

  private fun fileToDocument(
    file: File,
    repoDir: File,
    repositoryId: org.migor.feedless.repository.RepositoryId
  ): Document {
    val relativePath = file.relativeTo(repoDir).path
    val content = file.readText()
    val contentHash = calculateHash(content)

    val attrs = Files.readAttributes(file.toPath(), BasicFileAttributes::class.java)
    val createdAt = LocalDateTime.ofInstant(
      attrs.creationTime().toInstant(),
      ZoneId.systemDefault()
    )
    val updatedAt = LocalDateTime.ofInstant(
      attrs.lastModifiedTime().toInstant(),
      ZoneId.systemDefault()
    )

    // Determine MIME type based on file extension
    val mimeType = Files.probeContentType(file.toPath()) ?: "text/plain"

    return Document(
      url = "file://${file.absolutePath}",
      title = file.name,
      contentHash = contentHash,
      text = if (isTextFile(mimeType)) content else "",
      html = if (isHtmlFile(file)) content else null,
      raw = content.toByteArray(),
      rawMimeType = mimeType,
      tags = arrayOf(relativePath, file.extension),
      repositoryId = repositoryId,
      status = ReleaseStatus.released,
      createdAt = createdAt,
      updatedAt = updatedAt,
      publishedAt = updatedAt
    )
  }

  private fun calculateHash(content: String): String {
    val digest = MessageDigest.getInstance("SHA-256")
    val hashBytes = digest.digest(content.toByteArray())
    return hashBytes.joinToString("") { "%02x".format(it) }
  }

  private fun isTextFile(mimeType: String): Boolean {
    return mimeType.startsWith("text/") ||
      mimeType == "application/json" ||
      mimeType == "application/xml" ||
      mimeType == "application/javascript"
  }

  private fun isHtmlFile(file: File): Boolean {
    val extension = file.extension.lowercase()
    return extension == "html" || extension == "htm"
  }

  private fun applyFilters(documents: List<Document>, filter: DocumentsFilter): List<Document> {
    var result = documents

    // Filter by ID
    filter.id?.let { idFilter ->
      idFilter.eq?.let { id ->
        result = result.filter { it.id.uuid.toString() == id }
      }
      idFilter.`in`?.let { ids ->
        result = result.filter { ids.contains(it.id.uuid.toString()) }
      }
    }

    // Filter by tags
    filter.tags?.let { tagFilter ->
      tagFilter.eq?.let { tag ->
        result = result.filter { it.tags?.contains(tag) == true }
      }
      tagFilter.`in`?.let { tags ->
        result = result.filter { doc ->
          doc.tags?.any { tags.contains(it) } == true
        }
      }
    }

    // Filter by dates
    filter.createdAt?.let { dateFilter ->
      dateFilter.before?.let { before ->
        result = result.filter { it.createdAt.isBefore(before) }
      }
      dateFilter.after?.let { after ->
        result = result.filter { it.createdAt.isAfter(after) }
      }
    }

    filter.updatedAt?.let { dateFilter ->
      dateFilter.before?.let { before ->
        result = result.filter { it.updatedAt.isBefore(before) }
      }
      dateFilter.after?.let { after ->
        result = result.filter { it.updatedAt.isAfter(after) }
      }
    }

    filter.publishedAt?.let { dateFilter ->
      dateFilter.before?.let { before ->
        result = result.filter { it.publishedAt.isBefore(before) }
      }
      dateFilter.after?.let { after ->
        result = result.filter { it.publishedAt.isAfter(after) }
      }
    }

    filter.startedAt?.let { dateFilter ->
      dateFilter.before?.let { before ->
        result = result.filter { it.startingAt?.isBefore(before) == true }
      }
      dateFilter.after?.let { after ->
        result = result.filter { it.startingAt?.isAfter(after) == true }
      }
    }

    return result
  }

  private fun applyOrdering(documents: List<Document>, order: RecordOrderBy): List<Document> {
    return when (order.startedAt) {
      SortOrder.ASC -> documents.sortedBy { it.startingAt }
      SortOrder.DESC -> documents.sortedByDescending { it.startingAt }
      null -> documents
    }
  }

  private fun applyPagination(documents: List<Document>, pageable: PageableRequest): List<Document> {
    val offset = pageable.pageNumber * pageable.pageSize
    val end = (offset + pageable.pageSize).coerceAtMost(documents.size)

    return if (offset < documents.size) {
      documents.subList(offset, end)
    } else {
      emptyList()
    }
  }
}

