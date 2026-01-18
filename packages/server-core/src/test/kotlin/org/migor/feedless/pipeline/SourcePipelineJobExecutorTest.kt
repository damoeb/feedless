package org.migor.feedless.pipeline

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.migor.feedless.any
import org.migor.feedless.pipelineJob.PipelineJobId
import org.migor.feedless.pipelineJob.SourcePipelineJob
import org.migor.feedless.pipelineJob.SourcePipelineJobRepository
import org.migor.feedless.source.SourceId
import org.migor.feedless.source.SourceUseCase
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.scheduling.annotation.Scheduled
import java.time.LocalDateTime

class SourcePipelineJobExecutorTest {

  private lateinit var sourcePipelineJobExecutor: SourcePipelineJobExecutor
  private lateinit var sourceUseCase: SourceUseCase
  private lateinit var sourcePipelineJobRepository: SourcePipelineJobRepository

  @BeforeEach
  fun setUp() = runTest {
    sourceUseCase = mock(SourceUseCase::class.java)
    sourcePipelineJobRepository = mock(SourcePipelineJobRepository::class.java)

    sourcePipelineJobExecutor = SourcePipelineJobExecutor(
      sourceUseCase,
    )

    val id1 = SourceId()
    val id2 = SourceId()

    val sourceJobs = listOf(
      createSourcePipelineJob(id1),
      createSourcePipelineJob(id2),
      createSourcePipelineJob(id1)
    )
    `when`(sourcePipelineJobRepository.findAllPendingBatched(any(LocalDateTime::class.java))).thenReturn(sourceJobs)
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

  private fun createSourcePipelineJob(sourceId: SourceId): SourcePipelineJob {
    val job = mock(SourcePipelineJob::class.java)
    `when`(job.id).thenReturn(PipelineJobId())
    `when`(job.sourceId).thenReturn(sourceId)

//    val source = mock(SourceEntity::class.java)
//
//    `when`(sourceDAO.findById(eq(sourceId))).thenReturn(Optional.of(source))

    return job
  }

}
