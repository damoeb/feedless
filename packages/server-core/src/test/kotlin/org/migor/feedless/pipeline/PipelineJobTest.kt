package org.migor.feedless.pipeline

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.migor.feedless.document.DocumentDAO
import org.migor.feedless.document.DocumentEntity
import org.migor.feedless.document.eq
import org.migor.feedless.repository.RepositoryDAO
import org.migor.feedless.repository.RepositoryEntity
import org.migor.feedless.source.SourceDAO
import org.migor.feedless.source.SourceEntity
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness
import java.util.*

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PipelineJobTest {

  @Mock
  lateinit var documentDAO: DocumentDAO
  @Mock
  lateinit var pluginService: PluginService
  @Mock
  lateinit var sourceDAO: SourceDAO
  @Mock
  lateinit var repositoryDAO: RepositoryDAO
  @Mock
  lateinit var documentPipelineJobDAO: DocumentPipelineJobDAO
  @Mock
  lateinit var sourcePipelineJobDAO: SourcePipelineJobDAO

  @InjectMocks
  lateinit var pipelineJob: PipelineJob

  @BeforeEach
  fun setUp() {
    val repositoryId = UUID.randomUUID()
    val id1 = UUID.randomUUID()
    val id2 = UUID.randomUUID()
    val documentJobs = listOf(
      createDocumentPipelineJob(id1, repositoryId),
      createDocumentPipelineJob(id2, repositoryId),
      createDocumentPipelineJob(id1, repositoryId)
    )
    `when`(documentPipelineJobDAO.findAllPendingBatched()).thenReturn(documentJobs)

    val sourceJobs = listOf(
      createSourcePipelineJob(id1),
      createSourcePipelineJob(id2),
      createSourcePipelineJob(id1)
    )
    `when`(sourcePipelineJobDAO.findAllPendingBatched()).thenReturn(sourceJobs)
  }

  @Test
  fun processDocumentJobs() {
    pipelineJob.processDocumentJobs()
  }

  @Test
  fun processSourceJobs() {
    pipelineJob.processSourceJobs()
  }


  private fun createSourcePipelineJob(sourceId: UUID): SourcePipelineJobEntity {
    val jobId = UUID.randomUUID()
    val job = mock(SourcePipelineJobEntity::class.java)
    `when`(job.id).thenReturn(jobId)
    `when`(job.sourceId).thenReturn(sourceId)

    val source = mock(SourceEntity::class.java)

    `when`(sourceDAO.findById(eq(sourceId))).thenReturn(Optional.of(source))

    return job
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
