package org.migor.feedless.source

import jakarta.persistence.EntityManager
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.Mother.randomRepositoryId
import org.migor.feedless.Mother.randomUserId
import org.migor.feedless.any2
import org.migor.feedless.argThat
import org.migor.feedless.eq
import org.migor.feedless.generated.types.BoolUpdateOperationsInput
import org.migor.feedless.generated.types.DOMElementByXPathInput
import org.migor.feedless.generated.types.DOMExtractInput
import org.migor.feedless.generated.types.HttpFetchInput
import org.migor.feedless.generated.types.HttpGetRequestInput
import org.migor.feedless.generated.types.NullableUpdateFlowInput
import org.migor.feedless.generated.types.ScrapeActionInput
import org.migor.feedless.generated.types.ScrapeEmit
import org.migor.feedless.generated.types.ScrapeExtractInput
import org.migor.feedless.generated.types.ScrapeFlowInput
import org.migor.feedless.generated.types.SourceInput
import org.migor.feedless.generated.types.SourceUniqueWhereInput
import org.migor.feedless.generated.types.SourceUpdateDataInput
import org.migor.feedless.generated.types.SourceUpdateInput
import org.migor.feedless.generated.types.StringLiteralOrVariableInput
import org.migor.feedless.jpa.document.DocumentDAO
import org.migor.feedless.jpa.repository.RepositoryEntity
import org.migor.feedless.jpa.source.SourceDAO
import org.migor.feedless.jpa.source.SourceEntity
import org.migor.feedless.jpa.source.actions.ExtractEmit
import org.migor.feedless.jpa.source.actions.ExtractXpathActionEntity
import org.migor.feedless.jpa.source.actions.ScrapeActionDAO
import org.migor.feedless.jpa.source.actions.ScrapeActionEntity
import org.migor.feedless.jpa.sourcePipelineJob.SourcePipelineJobDAO
import org.migor.feedless.plan.PlanConstraintsService
import org.migor.feedless.repository.RepositoryHarvester
import org.migor.feedless.repository.RepositoryId
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.util.*

class SourceServiceTest {

  private lateinit var sourceDAO: SourceDAO
  private lateinit var scrapeActionDAO: ScrapeActionDAO
  private lateinit var sourceService: SourceService
  private lateinit var repository: RepositoryEntity
  private lateinit var repositoryId: RepositoryId

  @BeforeEach
  fun setUp() {
    sourceDAO = mock(SourceDAO::class.java)
    scrapeActionDAO = mock(ScrapeActionDAO::class.java)
    sourceService = SourceService(
      mock(SourcePipelineJobDAO::class.java),
      sourceDAO,
      mock(RepositoryHarvester::class.java),
      mock(DocumentDAO::class.java),
      mock(EntityManager::class.java),
      mock(PlanConstraintsService::class.java),
      scrapeActionDAO
    )

    repository = mock(RepositoryEntity::class.java)
    repositoryId = randomRepositoryId()
    `when`(repository.id).thenReturn(repositoryId.value)
  }

  @Test
  fun createSources() = runTest {

    val inputs = listOf(
      SourceInput(
        title = "wef",
        flow = ScrapeFlowInput(sequence = listOf(ScrapeActionInput(fetch = HttpFetchInput(get = HttpGetRequestInput(url = StringLiteralOrVariableInput())))))
      )
    )
    sourceService.createSources(randomUserId(), inputs, repository)

    verify(sourceDAO).saveAll(argThat<List<SourceEntity>> { it.size == 1 })
    verify(scrapeActionDAO).saveAll(argThat<List<ScrapeActionEntity>> { it.size == 1 })
  }

  @Test
  fun updateSources() = runTest {
    val sourceId = UUID.randomUUID()
    val update = SourceUpdateInput(
      where = SourceUniqueWhereInput(id = sourceId.toString()),
      data = SourceUpdateDataInput(
        disabled = BoolUpdateOperationsInput(
          set = false
        ),
        flow = NullableUpdateFlowInput(
          set = ScrapeFlowInput(
            sequence = listOf(
              ScrapeActionInput(
                fetch = HttpFetchInput(
                  get = HttpGetRequestInput(
                    url = StringLiteralOrVariableInput(
                      literal = "https::foo.bar"
                    )
                  )
                )
              ),
              ScrapeActionInput(
                extract = ScrapeExtractInput(
                  fragmentName = "foo",
                  selectorBased = DOMExtractInput(
                    fragmentName = "foo",
                    emit = listOf(ScrapeEmit.text, ScrapeEmit.pixel),
                    xpath = DOMElementByXPathInput("//bar"),
                    uniqueBy = ScrapeEmit.text
                  )
                )
              )
            )
          )
        )
      )
    )
    val source = mock(SourceEntity::class.java)
    `when`(source.repositoryId).thenReturn(repositoryId.value)
    `when`(source.id).thenReturn(sourceId)
    `when`(sourceDAO.findById(eq(sourceId))).thenReturn(Optional.of(source))

    `when`(scrapeActionDAO.findAllBySourceId(eq(sourceId))).thenReturn(listOf(mock(ScrapeActionEntity::class.java)))

    val updates = listOf(update)
    sourceService.updateSources(repository, updates)
    verify(scrapeActionDAO).deleteAll(any2())
    verify(scrapeActionDAO).saveAll(argThat<List<ScrapeActionEntity>> { it.size == 2 })
    verify(scrapeActionDAO).saveAll(argThat<List<ScrapeActionEntity>> {
      assertThat((it.get(1) as ExtractXpathActionEntity).getEmit()).isEqualTo(
        arrayOf(
          ExtractEmit.text,
          ExtractEmit.pixel
        )
      )
      true
    })
    verify(sourceDAO).saveAll(argThat<List<SourceEntity>> { it.size == 1 })
  }

  @Test
  fun deleteAllById() = runTest {
    val sources = listOf(
      UUID.randomUUID(),
      UUID.randomUUID(),
    ).map {
      val source = mock(SourceEntity::class.java)
      `when`(source.id).thenReturn(it)
      source
    }
    val sourceIds = sources.map { SourceId(it.id) }
    `when`(sourceDAO.findAllByRepositoryIdAndIdIn(repositoryId.value, sources.map { it.id })).thenReturn(sources)
    sourceService.deleteAllById(repositoryId, sourceIds)
    verify(sourceDAO).deleteAllById(argThat<List<UUID>> { it.size == sources.size })
  }
}
