package org.migor.feedless.pipeline

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.migor.feedless.document.DocumentEntity
import org.migor.feedless.document.DocumentService
import org.migor.feedless.repository.RepositoryService
import org.migor.feedless.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.scheduling.annotation.Scheduled
import java.time.LocalDateTime
import java.util.*

class DocumentPipelineJobExecutorTest {

  private lateinit var documentPipelineJobExecutor: DocumentPipelineJobExecutor
  private lateinit var documentPipelineService: DocumentPipelineService
  private lateinit var repositoryService: RepositoryService
  private lateinit var documentService: DocumentService

  @BeforeEach
  fun setUp() {
    documentPipelineService = mock(DocumentPipelineService::class.java)
    repositoryService = mock(RepositoryService::class.java)
    documentService = mock(DocumentService::class.java)

    documentPipelineJobExecutor = DocumentPipelineJobExecutor(
      documentPipelineService,
      repositoryService,
      documentService,
    )

    val repositoryId = UUID.randomUUID()
    val id1 = UUID.randomUUID()
    val id2 = UUID.randomUUID()
    val documentJobs = listOf(
      createDocumentPipelineJob(id1, repositoryId),
      createDocumentPipelineJob(id2, repositoryId),
      createDocumentPipelineJob(id1, repositoryId)
    )
    `when`(documentPipelineService.findAllPendingBatched(any(LocalDateTime::class.java))).thenReturn(documentJobs)
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

//    `when`(repositoryService.findById(eq(repositoryId))).thenReturn(Optional.of(mock(RepositoryEntity::class.java)))
//    `when`(documentDAO.findById(eq(documentId))).thenReturn(Optional.of(document))

    return job
  }
}
