package org.migor.feedless.jobs

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.migor.feedless.any2
import org.migor.feedless.common.CleanupExecutor
import org.migor.feedless.document.DocumentUseCase
import org.migor.feedless.harvest.HarvestRepository
import org.migor.feedless.pipelineJob.DocumentPipelineJobRepository
import org.migor.feedless.pipelineJob.SourcePipelineJobRepository
import org.migor.feedless.secrets.OneTimePasswordService
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness
import org.springframework.scheduling.annotation.Scheduled
import java.util.*

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CleanupExecutorTest {

  private lateinit var oneTimePasswordService: OneTimePasswordService
  private lateinit var sourcePipelineService: SourcePipelineJobRepository
  private lateinit var documentUseCase: DocumentUseCase
  private lateinit var documentPipelineJobRepository: DocumentPipelineJobRepository
  private lateinit var cleanupExecutor: CleanupExecutor
  private lateinit var harvestRepository: HarvestRepository

  @BeforeEach
  fun setUp() {

    oneTimePasswordService = mock(OneTimePasswordService::class.java)
    sourcePipelineService = mock(SourcePipelineJobRepository::class.java)
    documentUseCase = mock(DocumentUseCase::class.java)
    documentPipelineJobRepository = mock(DocumentPipelineJobRepository::class.java)
    harvestRepository = mock(HarvestRepository::class.java)
    cleanupExecutor = CleanupExecutor(
      Optional.of(oneTimePasswordService),
      sourcePipelineService,
      documentUseCase,
      documentPipelineJobRepository,
      harvestRepository
    )
  }

  @Test
  fun `verify executeCleanup is annotated with scheduled`() {
    val method = CleanupExecutor::class.java.declaredMethods.first { it.name == "executeCleanup" }
    assertThat(method.getAnnotation(Scheduled::class.java)).isNotNull()
  }

  @Test
  fun `executeCleanup removes oneTimePassword`() = runTest {
    cleanupExecutor.executeCleanup()
    verify(oneTimePasswordService, times(1)).deleteAllByValidUntilBefore(any2())
  }

  @Test
  fun `executeCleanup removes sourcePipelineJobs`() = runTest {
    cleanupExecutor.executeCleanup()
    verify(sourcePipelineService, times(1)).deleteAllByCreatedAtBefore(any2())
  }

  @Test
  fun `executeCleanup removes documentPipelineJobs`() = runTest {
    cleanupExecutor.executeCleanup()
    verify(documentPipelineJobRepository, times(1)).deleteAllByCreatedAtBefore(any2())
  }

  @Test
  fun `executeCleanup applies RetentionStrategy by capacity`() = runTest {
    cleanupExecutor.executeCleanup()
    verify(documentUseCase).applyRetentionStrategyByCapacity()
  }

  @Test
  fun `executeCleanup removes harvests`() {
    cleanupExecutor.executeCleanup()
    verify(harvestRepository).deleteAllTailingBySourceId()
  }

}
