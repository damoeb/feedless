package org.migor.feedless.pipeline

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.migor.feedless.any
import org.migor.feedless.data.jpa.document.DocumentEntity
import org.migor.feedless.document.DocumentId
import org.migor.feedless.document.DocumentUseCase
import org.migor.feedless.pipelineJob.DocumentPipelineJob
import org.migor.feedless.pipelineJob.DocumentPipelineJobRepository
import org.migor.feedless.pipelineJob.PipelineJobId
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.scheduling.annotation.Scheduled
import java.time.LocalDateTime
import java.util.*

class DocumentPipelineJobExecutorTest {

  private lateinit var documentPipelineJobExecutor: DocumentPipelineJobExecutor
  private lateinit var documentUseCase: DocumentUseCase
  private lateinit var documentPipelineJobRepository: DocumentPipelineJobRepository

  @BeforeEach
  fun setUp() = runTest {
    documentPipelineJobRepository = mock(DocumentPipelineJobRepository::class.java)
    documentUseCase = mock(DocumentUseCase::class.java)

    documentPipelineJobExecutor = DocumentPipelineJobExecutor(
      documentUseCase,
    )

    val repositoryId = UUID.randomUUID()
    val id1 = DocumentId(UUID.randomUUID())
    val id2 = DocumentId(UUID.randomUUID())
    val documentJobs = listOf(
      createDocumentPipelineJob(id1, repositoryId),
      createDocumentPipelineJob(id2, repositoryId),
      createDocumentPipelineJob(id1, repositoryId)
    )
    `when`(documentPipelineJobRepository.findAllPendingBatched(any(LocalDateTime::class.java))).thenReturn(documentJobs)
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

  private fun createDocumentPipelineJob(documentId: DocumentId, repositoryId: UUID): DocumentPipelineJob {
    val jobId = UUID.randomUUID()
    val job = mock(DocumentPipelineJob::class.java)
    `when`(job.id).thenReturn(PipelineJobId(jobId))
    `when`(job.documentId).thenReturn(documentId)

    val document = mock(DocumentEntity::class.java)
    `when`(document.id).thenReturn(documentId.uuid)
    `when`(document.repositoryId).thenReturn(repositoryId)

//    `when`(repositoryService.findById(eq(repositoryId))).thenReturn(Optional.of(mock(RepositoryEntity::class.java)))
//    `when`(documentDAO.findById(eq(documentId))).thenReturn(Optional.of(document))

    return job
  }
}
