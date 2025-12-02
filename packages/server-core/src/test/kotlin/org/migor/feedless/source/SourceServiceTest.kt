package org.migor.feedless.source

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.Mother.randomRepositoryId
import org.migor.feedless.actions.ExtractXpathAction
import org.migor.feedless.actions.FetchAction
import org.migor.feedless.actions.ScrapeAction
import org.migor.feedless.any2
import org.migor.feedless.argThat
import org.migor.feedless.document.DocumentRepository
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
import org.migor.feedless.pipelineJob.SourcePipelineJobRepository
import org.migor.feedless.plan.PlanConstraintsService
import org.migor.feedless.repository.Repository
import org.migor.feedless.repository.RepositoryHarvester
import org.migor.feedless.repository.RepositoryId
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class SourceServiceTest {

  private lateinit var sourceRepository: SourceRepository
  private lateinit var scrapeActionRepository: ScrapeActionRepository
  private lateinit var sourceUseCase: SourceUseCase
  private lateinit var repository: Repository
  private lateinit var repositoryId: RepositoryId

  @BeforeEach
  fun setUp() {
    sourceRepository = mock(SourceRepository::class.java)
    scrapeActionRepository = mock(ScrapeActionRepository::class.java)
    sourceUseCase = SourceUseCase(
      mock(SourcePipelineJobRepository::class.java),
      sourceRepository,
      mock(RepositoryHarvester::class.java),
      mock(DocumentRepository::class.java),
      mock(PlanConstraintsService::class.java),
      scrapeActionRepository
    )

    repository = mock(Repository::class.java)
    repositoryId = randomRepositoryId()
    `when`(repository.id).thenReturn(repositoryId)
  }

  @Test
  fun createSources() = runTest {

    val inputs = listOf(
      SourceInput(
        title = "wef",
        flow = ScrapeFlowInput(
          sequence = listOf(
            ScrapeActionInput(
              fetch = HttpFetchInput(
                get = HttpGetRequestInput(
                  url = StringLiteralOrVariableInput()
                )
              )
            )
          )
        )
      )
    )
    sourceUseCase.createSources(inputs, repositoryId)

    verify(sourceRepository).saveAll(argThat<List<Source>> { it.size == 1 })
    verify(scrapeActionRepository).saveAll(argThat<List<ScrapeAction>> { it.size == 1 })
  }

  @Test
  fun updateSources() = runTest {
    val sourceId = SourceId()
    val update = SourceUpdateInput(
      where = SourceUniqueWhereInput(id = sourceId.uuid.toString()),
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
    val source = mock(Source::class.java)
    `when`(source.repositoryId).thenReturn(repositoryId)
    `when`(source.id).thenReturn(sourceId)
    `when`(sourceRepository.findById(eq(sourceId))).thenReturn(source)

    `when`(scrapeActionRepository.findAllBySourceId(eq(sourceId))).thenReturn(listOf(mock(FetchAction::class.java)))

    val updates = listOf(update)

    sourceUseCase.updateSources(repositoryId, updates)

    verify(scrapeActionRepository).deleteAll(any2())
    verify(scrapeActionRepository).saveAll(argThat { it.size == 2 })
    verify(scrapeActionRepository).saveAll(argThat {
      assertThat((it.get(1) as ExtractXpathAction).emit).isEqualTo(
        arrayOf(
          ExtractEmit.text,
          ExtractEmit.pixel
        )
      )
      true
    })
    verify(sourceRepository).saveAll(argThat { it.size == 1 })
  }

  @Test
  fun deleteAllById() = runTest {
    val sources = listOf(
      SourceId(),
      SourceId(),
    ).map {
      val source = mock(Source::class.java)
      `when`(source.id).thenReturn(it)
      source
    }
    val sourceIds = sources.map { it.id }
    `when`(sourceRepository.findAllByRepositoryIdAndIdIn(repositoryId, sources.map { it.id })).thenReturn(sources)
    sourceUseCase.deleteAllById(repositoryId, sourceIds)
    verify(sourceRepository).deleteAllById(argThat<List<SourceId>> { it.size == sources.size })
  }
}
