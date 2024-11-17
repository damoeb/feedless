package org.migor.feedless.pipeline

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.migor.feedless.document.DocumentDAO
import org.migor.feedless.document.DocumentEntity
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
import java.time.LocalDateTime
import java.util.*

class SourcePipelineJobExecutorTest {

  private lateinit var sourceDAO: SourceDAO
  private lateinit var repositoryDAO: RepositoryDAO
  private lateinit var sourcePipelineJobDAO: SourcePipelineJobDAO
  private lateinit var sourcePipelineJobExecutor: SourcePipelineJobExecutor

  @BeforeEach
  fun setUp() {
    sourcePipelineJobDAO = mock(SourcePipelineJobDAO::class.java)
    sourceDAO = mock(SourceDAO::class.java)
    repositoryDAO = mock(RepositoryDAO::class.java)

    sourcePipelineJobExecutor = SourcePipelineJobExecutor(
      sourceDAO,
      sourcePipelineJobDAO,
      repositoryDAO,
      mock(SourceService::class.java)
    )

    val id1 = UUID.randomUUID()
    val id2 = UUID.randomUUID()

    val sourceJobs = listOf(
      createSourcePipelineJob(id1),
      createSourcePipelineJob(id2),
      createSourcePipelineJob(id1)
    )
    `when`(sourcePipelineJobDAO.findAllPendingBatched(any(LocalDateTime::class.java))).thenReturn(sourceJobs)
  }

  @Test
  fun `verify processSourceJobs is annotated with scheduled`() {
    val method = sourcePipelineJobExecutor::class.java.declaredMethods.first { it.name == "processSourceJobs" }
    assertThat(method.getAnnotation(Scheduled::class.java)).isNotNull()
  }

  @Test
  @Disabled
  fun `processSourceJobs will increment attempt count and terminate`() {
    // todo test
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

}
