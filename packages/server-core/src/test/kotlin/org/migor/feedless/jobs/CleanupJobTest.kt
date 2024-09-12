package org.migor.feedless.jobs

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.migor.feedless.common.CleanupExecutor
import org.migor.feedless.mail.OneTimePasswordDAO
import org.migor.feedless.pipeline.DocumentPipelineJobDAO
import org.migor.feedless.pipeline.SourcePipelineJobDAO
import org.migor.feedless.repository.HarvestDAO
import org.migor.feedless.repository.any
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CleanupJobTest {

  private lateinit var oneTimePasswordDAO: OneTimePasswordDAO
  private lateinit var sourcePipelineJobDAO: SourcePipelineJobDAO
  private lateinit var harvestDAO: HarvestDAO
  private lateinit var documentPipelineJobDAO: DocumentPipelineJobDAO

  private lateinit var cleanupExecutor: CleanupExecutor

  @BeforeEach
  fun setUp() {

    oneTimePasswordDAO = mock(OneTimePasswordDAO::class.java)
    sourcePipelineJobDAO = mock(SourcePipelineJobDAO::class.java)
    harvestDAO = mock(HarvestDAO::class.java)
    documentPipelineJobDAO = mock(DocumentPipelineJobDAO::class.java)
    cleanupExecutor = CleanupExecutor(
      oneTimePasswordDAO,
      sourcePipelineJobDAO,
      harvestDAO,
      documentPipelineJobDAO
    )
  }

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
