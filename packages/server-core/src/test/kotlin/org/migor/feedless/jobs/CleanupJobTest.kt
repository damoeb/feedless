package org.migor.feedless.jobs

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.migor.feedless.document.any
import org.migor.feedless.pipeline.DocumentPipelineJobDAO
import org.migor.feedless.pipeline.SourcePipelineJobDAO
import org.migor.feedless.secrets.OneTimePasswordDAO
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.times
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness
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

  @InjectMocks
  lateinit var cleanupExecutor: CleanupExecutor

  @Test
  fun `executeCleanup removes oneTimePassword`() {
    cleanupExecutor.executeCleanup()
    Mockito.verify(oneTimePasswordDAO, times(1)).deleteAllByValidUntilBefore(any(Date::class.java))
  }

  @Test
  fun `executeCleanup removes sourcePipelineJobs`() {
    cleanupExecutor.executeCleanup()
    Mockito.verify(sourcePipelineJobDAO, times(1)).deleteAllByCreatedAtBefore(any(Date::class.java))
  }

  @Test
  fun `executeCleanup removes documentPipelineJobs`() {
    cleanupExecutor.executeCleanup()
    Mockito.verify(documentPipelineJobDAO, times(1)).deleteAllByCreatedAtBefore(any(Date::class.java))
  }
}
