package org.migor.feedless.source

import jakarta.persistence.EntityManager
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.migor.feedless.actions.ScrapeActionDAO
import org.migor.feedless.actions.ScrapeActionEntity
import org.migor.feedless.document.DocumentDAO
import org.migor.feedless.generated.types.HttpFetchInput
import org.migor.feedless.generated.types.HttpGetRequestInput
import org.migor.feedless.generated.types.ScrapeActionInput
import org.migor.feedless.generated.types.ScrapeFlowInput
import org.migor.feedless.generated.types.SourceInput
import org.migor.feedless.generated.types.StringLiteralOrVariableInput
import org.migor.feedless.pipeline.SourcePipelineJobDAO
import org.migor.feedless.plan.PlanConstraintsService
import org.migor.feedless.repository.RepositoryEntity
import org.migor.feedless.repository.RepositoryHarvester
import org.migor.feedless.repository.any2
import org.migor.feedless.repository.argThat
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.util.*

class SourceServiceTest {

  @Test
  fun createSources() = runTest {
    val sourceDAO = mock(SourceDAO::class.java)
    val scrapeActionDAO = mock(ScrapeActionDAO::class.java)
    val sourceService = SourceService(
      mock(SourcePipelineJobDAO::class.java),
      sourceDAO,
      mock(RepositoryHarvester::class.java),
      mock(DocumentDAO::class.java),
      mock(EntityManager::class.java),
      mock(PlanConstraintsService::class.java),
      scrapeActionDAO
    )
    val repository = mock(RepositoryEntity::class.java)
    `when`(repository.id).thenReturn(UUID.randomUUID())

    val inputs = listOf(
      SourceInput(
        title = "wef",
        flow = ScrapeFlowInput(sequence = listOf(ScrapeActionInput(fetch = HttpFetchInput(get = HttpGetRequestInput(url = StringLiteralOrVariableInput())))))
      )
    )
    sourceService.createSources(UUID.randomUUID(), inputs, repository)

    verify(sourceDAO).saveAll(argThat<List<SourceEntity>> { it.size == 1 })
    verify(scrapeActionDAO).saveAll(argThat<List<ScrapeActionEntity>> { it.size == 1 })
  }

  @Test
  @Disabled
  fun updateSources() {
  }

  @Test
  @Disabled
  fun deleteAllById() {
  }
}
