package org.migor.feedless.jobs

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.migor.feedless.pipeline.DocumentPipelineJobDAO
import org.migor.feedless.pipeline.SourcePipelineJobDAO
import org.migor.feedless.repository.HarvestDAO
import org.migor.feedless.repository.any
import org.migor.feedless.secrets.OneTimePasswordDAO
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.times
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CleanupJobTest {

  @Mock
  lateinit var oneTimePasswordDAO: OneTimePasswordDAO

  @Mock
  lateinit var sourcePipelineJobDAO: SourcePipelineJobDAO

  @Mock
  lateinit var documentPipelineJobDAO: DocumentPipelineJobDAO

  @Mock
  lateinit var harvestDAO: HarvestDAO

  @InjectMocks
  lateinit var cleanupExecutor: CleanupExecutor

  @Test
  fun `executeCleanup removes trailing harvests`() {
    cleanupExecutor.executeCleanup()
    Mockito.verify(harvestDAO, times(1)).deleteAllTailingByRepositoryId()
  }

  @Test
  fun `executeCleanup removes oneTimePassword`() {
    cleanupExecutor.executeCleanup()
    Mockito.verify(oneTimePasswordDAO, times(1)).deleteAllByValidUntilBefore(any(LocalDateTime::class.java))
  }

  @Test
  fun `executeCleanup removes sourcePipelineJobs`() {
    cleanupExecutor.executeCleanup()
    Mockito.verify(sourcePipelineJobDAO, times(1)).deleteAllByCreatedAtBefore(any(LocalDateTime::class.java))
  }

  @Test
  fun `executeCleanup removes documentPipelineJobs`() {
    cleanupExecutor.executeCleanup()
    Mockito.verify(documentPipelineJobDAO, times(1)).deleteAllByCreatedAtBefore(any(LocalDateTime::class.java))
  }
}
