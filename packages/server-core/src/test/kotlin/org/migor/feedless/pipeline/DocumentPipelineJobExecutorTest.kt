package org.migor.feedless.pipeline

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.migor.feedless.document.DocumentDAO
import org.migor.feedless.document.DocumentEntity
import org.migor.feedless.document.DocumentService
import org.migor.feedless.repository.RepositoryDAO
import org.migor.feedless.repository.RepositoryEntity
import org.migor.feedless.repository.any
import org.migor.feedless.repository.eq
import org.migor.feedless.source.SourceDAO
import org.migor.feedless.source.SourceEntity
import org.migor.feedless.source.SourceService
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.transaction.PlatformTransactionManager
import java.time.LocalDateTime
import java.util.*

class DocumentPipelineJobExecutorTest {

  private lateinit var documentDAO: DocumentDAO
  private lateinit var repositoryDAO: RepositoryDAO
  private lateinit var documentPipelineJobDAO: DocumentPipelineJobDAO
  private lateinit var documentPipelineJobExecutor: DocumentPipelineJobExecutor

  @BeforeEach
  fun setUp() {
    documentPipelineJobDAO = mock(DocumentPipelineJobDAO::class.java)
    documentDAO = mock(DocumentDAO::class.java)
    repositoryDAO = mock(RepositoryDAO::class.java)

    documentPipelineJobExecutor = DocumentPipelineJobExecutor(
      documentPipelineJobDAO,
      documentDAO,
      repositoryDAO,
      mock(DocumentService::class.java),
    )

    val repositoryId = UUID.randomUUID()
    val id1 = UUID.randomUUID()
    val id2 = UUID.randomUUID()
    val documentJobs = listOf(
      createDocumentPipelineJob(id1, repositoryId),
      createDocumentPipelineJob(id2, repositoryId),
      createDocumentPipelineJob(id1, repositoryId)
    )
    `when`(documentPipelineJobDAO.findAllPendingBatched(any(LocalDateTime::class.java))).thenReturn(documentJobs)
  }

  @Test
  fun `verify processDocumentJobs is annotated with scheduled`() {
    val method = documentPipelineJobExecutor::class.java.declaredMethods.first { it.name == "processDocumentJobs" }
    assertThat(method.getAnnotation(Scheduled::class.java)).isNotNull()
  }

  @Test
  @Disabled
  fun `processDocumentJobs will increment attempt count and terminate`() {
    // todo test
  }

  private fun createDocumentPipelineJob(documentId: UUID, repositoryId: UUID): DocumentPipelineJobEntity {
    val jobId = UUID.randomUUID()
    val job = mock(DocumentPipelineJobEntity::class.java)
    `when`(job.id).thenReturn(jobId)
    `when`(job.documentId).thenReturn(documentId)

    val document = mock(DocumentEntity::class.java)
    `when`(document.id).thenReturn(documentId)
    `when`(document.repositoryId).thenReturn(repositoryId)

    `when`(repositoryDAO.findById(eq(repositoryId))).thenReturn(Optional.of(mock(RepositoryEntity::class.java)))
    `when`(documentDAO.findById(eq(documentId))).thenReturn(Optional.of(document))

    return job
  }
}
