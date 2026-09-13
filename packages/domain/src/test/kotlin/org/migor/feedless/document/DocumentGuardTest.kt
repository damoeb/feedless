package org.migor.feedless.document

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.migor.feedless.Mother.randomDocumentId
import org.migor.feedless.NotFoundException
import org.migor.feedless.repository.Repository
import org.migor.feedless.repository.RepositoryGuard
import org.migor.feedless.repository.RepositoryId
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDateTime

class DocumentGuardTest {

  private val documentRepository = mock<DocumentRepository>()
  private val repositoryGuard = mock<RepositoryGuard>()
  private val guard = DocumentGuard(documentRepository, repositoryGuard)
  private val document = Document(
    url = "https://example.org/a",
    text = "text",
    repositoryId = RepositoryId(),
    status = ReleaseStatus.released,
    publishedAt = LocalDateTime.now(),
    contentHash = "",
  )

  @Test
  fun `requireRead returns a document of a repository the caller may read`() = runTest {
    whenever(documentRepository.findById(document.id)).thenReturn(document)
    whenever(repositoryGuard.requireRead(document.repositoryId)).thenReturn(mock<Repository>())

    assertThat(guard.requireRead(document.id)).isEqualTo(document)
  }

  @Test
  fun `requireRead of a document in a repository the caller may not read answers like a missing document`() = runTest {
    whenever(documentRepository.findById(document.id)).thenReturn(document)
    whenever(repositoryGuard.requireRead(document.repositoryId))
      .thenThrow(NotFoundException("Repository ${document.repositoryId} not found"))
    val missingId = randomDocumentId()

    val denied = runCatching { guard.requireRead(document.id) }.exceptionOrNull()
    val missing = runCatching { guard.requireRead(missingId) }.exceptionOrNull()

    assertThat(denied).isInstanceOf(NotFoundException::class.java).hasMessage("Document ${document.id} not found")
    assertThat(missing).isInstanceOf(NotFoundException::class.java).hasMessage("Document $missingId not found")
  }
}
