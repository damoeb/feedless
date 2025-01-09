package org.migor.feedless.jobs

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.migor.feedless.common.CleanupExecutor
import org.migor.feedless.document.DocumentService
import org.migor.feedless.mail.OneTimePasswordService
import org.migor.feedless.pipeline.DocumentPipelineService
import org.migor.feedless.pipeline.SourcePipelineService
import org.migor.feedless.repository.HarvestService
import org.migor.feedless.repository.any2
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
  private lateinit var sourcePipelineService: SourcePipelineService
  private lateinit var documentService: DocumentService
  private lateinit var documentPipelineService: DocumentPipelineService
  private lateinit var cleanupExecutor: CleanupExecutor
  private lateinit var harvestService: HarvestService

  @BeforeEach
  fun setUp() {

    oneTimePasswordService = mock(OneTimePasswordService::class.java)
    sourcePipelineService = mock(SourcePipelineService::class.java)
    documentService = mock(DocumentService::class.java)
    documentPipelineService = mock(DocumentPipelineService::class.java)
    harvestService = mock(HarvestService::class.java)
    cleanupExecutor = CleanupExecutor(
      Optional.of(oneTimePasswordService),
      sourcePipelineService,
      documentService,
      documentPipelineService,
      harvestService
    )
  }

  @Test
  fun `verify executeCleanup is annotated with scheduled`() {
    val method = CleanupExecutor::class.java.declaredMethods.first { it.name == "executeCleanup" }
    assertThat(method.getAnnotation(Scheduled::class.java)).isNotNull()
  }

  @Test
  fun `executeCleanup removes oneTimePassword`() {
    cleanupExecutor.executeCleanup()
    verify(oneTimePasswordService, times(1)).deleteAllByValidUntilBefore(any2())
  }

  @Test
  fun `executeCleanup removes sourcePipelineJobs`() {
    cleanupExecutor.executeCleanup()
    verify(sourcePipelineService, times(1)).deleteAllByCreatedAtBefore(any2())
  }

  @Test
  fun `executeCleanup removes documentPipelineJobs`() {
    cleanupExecutor.executeCleanup()
    verify(documentPipelineService, times(1)).deleteAllByCreatedAtBefore(any2())
  }

  @Test
  fun `executeCleanup applies RetentionStrategy by capacity`() {
    cleanupExecutor.executeCleanup()
    verify(documentService).applyRetentionStrategyByCapacity()
  }

  @Test
  fun `executeCleanup removes harvests`() {
    cleanupExecutor.executeCleanup()
    verify(harvestService).deleteAllTailing()
  }

}
