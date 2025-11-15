package org.migor.feedless.pipeline

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.migor.feedless.any
import org.migor.feedless.jpa.sourcePipelineJob.SourcePipelineJobEntity
import org.migor.feedless.repository.RepositoryService
import org.migor.feedless.source.SourceService
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.scheduling.annotation.Scheduled
import java.time.LocalDateTime
import java.util.*

class SourcePipelineJobExecutorTest {

  private lateinit var sourcePipelineJobExecutor: SourcePipelineJobExecutor
  private lateinit var sourcePipelineService: SourcePipelineService
  private lateinit var repositoryService: RepositoryService
  private lateinit var sourceService: SourceService

  @BeforeEach
  fun setUp() {
    sourcePipelineService = mock(SourcePipelineService::class.java)
    repositoryService = mock(RepositoryService::class.java)
    sourceService = mock(SourceService::class.java)

    sourcePipelineJobExecutor = SourcePipelineJobExecutor(
      sourcePipelineService,
      repositoryService,
      sourceService,
    )

    val id1 = UUID.randomUUID()
    val id2 = UUID.randomUUID()

    val sourceJobs = listOf(
      createSourcePipelineJob(id1),
      createSourcePipelineJob(id2),
      createSourcePipelineJob(id1)
    )
    `when`(sourcePipelineService.findAllPendingBatched(any(LocalDateTime::class.java))).thenReturn(sourceJobs)
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

//    val source = mock(SourceEntity::class.java)
//
//    `when`(sourceDAO.findById(eq(sourceId))).thenReturn(Optional.of(source))

    return job
  }

}
