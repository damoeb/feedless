package org.migor.feedless.repository

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.migor.feedless.Mother.randomRepositoryId
import org.migor.feedless.Mother.randomUserId
import org.migor.feedless.PageableRequest
import org.migor.feedless.ResumableHarvestException
import org.migor.feedless.Vertical
import org.migor.feedless.actions.PluginExecutionJson
import org.migor.feedless.any
import org.migor.feedless.any2
import org.migor.feedless.anyList
import org.migor.feedless.argThat
import org.migor.feedless.document.Document
import org.migor.feedless.document.DocumentId
import org.migor.feedless.document.DocumentRepository
import org.migor.feedless.document.DocumentUseCase
import org.migor.feedless.eq
import org.migor.feedless.feed.parser.json.JsonItem
import org.migor.feedless.feed.parser.json.JsonPoint
import org.migor.feedless.generated.types.FeedlessPlugins
import org.migor.feedless.generated.types.MimeData
import org.migor.feedless.generated.types.ScrapeExtractFragment
import org.migor.feedless.generated.types.ScrapeExtractFragmentPart
import org.migor.feedless.generated.types.TextData
import org.migor.feedless.group.GroupId
import org.migor.feedless.harvest.HarvestRepository
import org.migor.feedless.pipeline.FragmentOutput
import org.migor.feedless.pipelineJob.DocumentPipelineJobRepository
import org.migor.feedless.pipelineJob.PluginExecution
import org.migor.feedless.pipelineJob.SourcePipelineJob
import org.migor.feedless.pipelineJob.SourcePipelineJobRepository
import org.migor.feedless.scrape.LogCollector
import org.migor.feedless.scrape.ScrapeActionOutput
import org.migor.feedless.scrape.ScrapeOutput
import org.migor.feedless.scrape.ScrapeService
import org.migor.feedless.scrape.WebExtractService.Companion.MIME_URL
import org.migor.feedless.session.RequestContext
import org.migor.feedless.source.Source
import org.migor.feedless.source.SourceId
import org.migor.feedless.source.SourceRepository
import org.migor.feedless.user.UserId
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness
import java.time.Duration
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RepositoryHarvesterTest {

  private lateinit var documentUseCase: DocumentUseCase
  private lateinit var sourceRepository: SourceRepository
  private lateinit var meterRegistry: MeterRegistry
  private lateinit var repositoryUseCase: RepositoryUseCase
  private lateinit var scrapeService: ScrapeService
  private lateinit var repositoryHarvester: RepositoryHarvester
  private lateinit var repositoryId: RepositoryId

  private lateinit var repository: Repository
  private lateinit var source: Source
  private lateinit var sourcePipelineJobRepository: SourcePipelineJobRepository
  private lateinit var documentPipelineJobRepository: DocumentPipelineJobRepository
  private lateinit var documentRepository: DocumentRepository
  private lateinit var repositoryRepository: RepositoryRepository

  @BeforeEach
  fun setUp() = runTest {
    repositoryId = randomRepositoryId()
    documentUseCase = mock(DocumentUseCase::class.java)
    sourceRepository = mock(SourceRepository::class.java)
    meterRegistry = mock(MeterRegistry::class.java)
    repositoryUseCase = mock(RepositoryUseCase::class.java)
    scrapeService = mock(ScrapeService::class.java)
    sourcePipelineJobRepository = mock(SourcePipelineJobRepository::class.java)
    documentPipelineJobRepository = mock(DocumentPipelineJobRepository::class.java)
    documentRepository = mock(DocumentRepository::class.java)
    repositoryRepository = mock(RepositoryRepository::class.java)

    repositoryHarvester = RepositoryHarvester(
      documentUseCase,
      documentRepository,
      documentPipelineJobRepository,
      sourcePipelineJobRepository,
      sourceRepository,
      scrapeService,
      meterRegistry,
      repositoryUseCase,
      repositoryRepository,
      mock(HarvestRepository::class.java),
    )

    `when`(meterRegistry.counter(any2(), anyList())).thenReturn(mock(Counter::class.java))
    `when`(meterRegistry.counter(any2())).thenReturn(mock(Counter::class.java))

    source = mock(Source::class.java)
    `when`(source.disabled).thenReturn(false)
    `when`(source.id).thenReturn(SourceId())
    `when`(source.repositoryId).thenReturn(repositoryId)
    `when`(source.errorsInSuccession).thenReturn(0)

    repository = mock(Repository::class.java)
    `when`(repository.id).thenReturn(repositoryId)
    `when`(repository.sourcesSyncCron).thenReturn("")
    `when`(repository.ownerId).thenReturn(UserId())
    `when`(repository.product).thenReturn(Vertical.feedless)
    `when`(repository.plugins).thenReturn(emptyList())

    `when`(sourceRepository.findAllByRepositoryIdFiltered(any2(), any2(), eq(null), eq(null))).thenAnswer {
      if ((it.arguments[1] as PageableRequest).pageNumber == 0) {
        mutableListOf(source)
      } else {
        emptyList()
      }
    }

    `when`(repositoryUseCase.findById(eq(repositoryId))).thenReturn(repository)
    `when`(repositoryRepository.findById(eq(repositoryId))).thenReturn(repository)

    `when`(
      repositoryUseCase.calculateScheduledNextAt(
        any(String::class.java), any(GroupId::class.java), any(
          LocalDateTime::class.java
        )
      )
    ).thenReturn(LocalDateTime.now())
  }

  @Test
  fun `given scrape fails will increment the error count`() = runTest {
    `when`(
      scrapeService.scrape(
        any2(),
        any2()
      )
    ).thenThrow(
      IllegalArgumentException("this is off")
    )
    `when`(source.errorsInSuccession).thenReturn(0)

    repositoryHarvester.harvestRepository(repositoryId)

    verify(scrapeService, times(1)).scrape(
      any2(),
      any2()
    )

    verify(sourceRepository, times(1)).save(
      source
        .copy(
          disabled = false,
          errorsInSuccession = 1,
          lastErrorMessage = "this is off"
        )
    )
  }

  @Test
  @Disabled
  fun `given scrape works, errorCount will be reset`() = runTest {
    `when`(source.errorsInSuccession).thenReturn(3)
    `when`(
      scrapeService.scrape(
        any(Source::class.java),
        any(LogCollector::class.java)
      )
    ).thenReturn(
      ScrapeOutput(outputs = emptyList(), time = 0)
    )

    repositoryHarvester.harvestRepository(repositoryId)

    verify(sourceRepository, times(1))
      .save(
        source.copy(
          errorsInSuccession = 0,
          lastErrorMessage = null
        )
      )
  }

  @Test
  @Disabled("feature is disabled")
  fun `given scrape fails will disable source once error-count threshold is met`() = runTest {
    `when`(
      scrapeService.scrape(
        any(Source::class.java),
        any(LogCollector::class.java)
      )
    ).thenThrow(
      IllegalArgumentException("this is off")
    )
    `when`(source.errorsInSuccession).thenReturn(4)

    repositoryHarvester.harvestRepository(repositoryId)

    verify(scrapeService, times(1)).scrape(
      any(Source::class.java),
      any(LogCollector::class.java)
    )

    verify(sourceRepository, times(1))
      .save(
        source.copy(
          disabled = true,
          errorsInSuccession = 5,
          lastErrorMessage = "this is off"
        )
      )
  }

  @Test
  fun `given scrape fails recoverable will not flag the source errornous`() = runTest {
    // given
    assertThat(source.errorsInSuccession).isEqualTo(0)
    `when`(
      scrapeService.scrape(
        any(Source::class.java),
        any(LogCollector::class.java)
      )
    ).thenThrow(
      ResumableHarvestException("they warned us about this", Duration.ofMinutes(5))
    )

    // when
    repositoryHarvester.harvestRepository(repositoryId)

    // then
    assertThat(source.errorsInSuccession).isEqualTo(0)
    verify(scrapeService, times(1)).scrape(
      any(Source::class.java),
      any(LogCollector::class.java)
    )

    verify(sourceRepository, times(1))
      .save(
        source.copy(
          errorsInSuccession = 0,
          lastErrorMessage = "they warned us about this"
        )
      )
  }

  @Test
  fun `given documents feature a url, then urls will be used to deduplicate`() =
    runTest(context = RequestContext(groupId = GroupId(), userId = randomUserId())) {
      `when`(
        scrapeService.scrape(
          any(Source::class.java),
          any(LogCollector::class.java)
        )
      ).thenReturn(
        ScrapeOutput(
          outputs = listOf(
            ScrapeActionOutput(
              index = 0,
              fragment = FragmentOutput(
                fragmentName = "feed",
                fragments = emptyList(),
                items = listOf(
                  newJsonItem(url = "https://example.org/1", title = "3"),
                  newJsonItem(url = "https://example.org/1", title = "3"),
                  newJsonItem(url = "https://example.org/3", title = "3"),
                  newJsonItem(url = "https://example.org/4", title = "3"),
                )
              )
            )
          ),
          time = 0
        )
      )

      repositoryHarvester.harvestRepository(repositoryId)

      verify(documentRepository).saveAll(argThat { it.count() == 3 })
    }

  @Test
  fun `given documents feature fragments, the fragments will be persisted`() =
    runTest(context = RequestContext(groupId = GroupId(), userId = randomUserId())) {

      `when`(
        documentUseCase.findFirstByContentHashOrUrlAndRepositoryId(
          any(String::class.java),
          any(String::class.java),
          any(RepositoryId::class.java)
        )
      ).thenReturn(null)
      `when`(
        scrapeService.scrape(
          any(Source::class.java),
          any(LogCollector::class.java)
        )
      ).thenReturn(
        ScrapeOutput(
          outputs = listOf(
            ScrapeActionOutput(
              index = 0,
              fragment = FragmentOutput(
                fragmentName = "feed",
                fragments = listOf(
                  ScrapeExtractFragment(
                    data = MimeData(mimeType = "image/png", data = "aGFsbG8K"),
                    html = TextData(data = "html"),
                    text = TextData(data = "text"),
                    uniqueBy = ScrapeExtractFragmentPart.html
                  )
                ),
                items = emptyList(),
              )
            )
          ),
          time = 0
        )
      )

      repositoryHarvester.harvestRepository(repositoryId)

      verify(documentRepository).saveAll(argThat { it.count() == 1 })
    }

  @Test
  fun `given documents feature no url, then titles will be used to deduplicate`() =
    runTest(context = RequestContext(groupId = GroupId(), userId = randomUserId())) {
      `when`(
        scrapeService.scrape(
          any(Source::class.java),
          any(LogCollector::class.java)
        )
      ).thenReturn(
        ScrapeOutput(
          outputs = listOf(
            ScrapeActionOutput(
              index = 0,
              fragment = FragmentOutput(
                fragmentName = "feed",
                fragments = emptyList(),
                items = listOf(
                  newJsonItem(url = "", title = "1"),
                  newJsonItem(url = "", title = "1"),
                  newJsonItem(url = "", title = "1"),
                  newJsonItem(url = "", title = "4"),
                )
              )
            )
          ),
          time = 0
        )
      )

      repositoryHarvester.harvestRepository(repositoryId)

      verify(documentRepository).saveAll(argThat { it.count() == 2 })
    }

  @Test
  fun `updates for existing documents will be ignored, if repository has plugins`() =
    runTest(context = RequestContext(groupId = GroupId(), userId = randomUserId())) {
      `when`(repository.plugins).thenReturn(listOf(mock(PluginExecution::class.java)))
      val existing = mock(Document::class.java)
      `when`(
        documentUseCase.findFirstByContentHashOrUrlAndRepositoryId(
          any(String::class.java),
          any(String::class.java),
          any(RepositoryId::class.java)
        )
      ).thenReturn(
        existing
      )

      `when`(
        scrapeService.scrape(
          any(Source::class.java),
          any(LogCollector::class.java)
        )
      ).thenReturn(
        ScrapeOutput(
          outputs = listOf(
            ScrapeActionOutput(
              index = 0,
              fragment = FragmentOutput(
                fragmentName = "feed",
                fragments = emptyList(),
                items = listOf(
                  newJsonItem(url = "", title = "updated.title"),
                )
              )
            )
          ),
          time = 0
        )
      )

      repositoryHarvester.harvestRepository(repositoryId)

      verify(documentRepository).saveAll(argThat {
        it.isEmpty()
      })
    }

  private fun createPlugin(): PluginExecution {
    return PluginExecution(FeedlessPlugins.org_feedless_fulltext.name, PluginExecutionJson())
  }

  @Test
  @Disabled("lastUpdateAt is polluted and cannot be used atm")
  fun `updates for existing documents will be processed, if repository has changed after existing has been created`() =
    runTest(context = RequestContext(groupId = GroupId(), userId = randomUserId())) {
      `when`(repository.plugins).thenReturn(listOf(createPlugin(), createPlugin()))
      val existing = mock(Document::class.java)
      `when`(existing.id).thenReturn(DocumentId())
      `when`(
        documentUseCase.findFirstByContentHashOrUrlAndRepositoryId(
          any(String::class.java),
          any(String::class.java),
          any(RepositoryId::class.java)
        )
      ).thenReturn(
        existing
      )

      val date = LocalDateTime.now()
      `when`(repository.lastUpdatedAt).thenReturn(date)
      `when`(existing.createdAt).thenReturn(date.minusMinutes(1))

      `when`(
        scrapeService.scrape(
          any(Source::class.java),
          any(LogCollector::class.java)
        )
      ).thenReturn(
        ScrapeOutput(
          outputs = listOf(
            ScrapeActionOutput(
              index = 0,
              fragment = FragmentOutput(
                fragmentName = "feed",
                fragments = emptyList(),
                items = listOf(
                  newJsonItem(url = "", title = "updated.title"),
                )
              )
            )
          ),
          time = 0
        )
      )

      repositoryHarvester.harvestRepository(repositoryId)

      verify(documentPipelineJobRepository).deleteAllByDocumentIdIn(argThat {
        it.count() == 1
      })
      verify(documentPipelineJobRepository).saveAll(argThat {
        it.count() == 2 // number of plugins
      })
//         TODO   verify(existing).status = ReleaseStatus.unreleased
    }

  @Test
  fun `released documents will trigger post release effects`() =
    runTest(context = RequestContext(groupId = GroupId(), userId = randomUserId())) {
      `when`(repository.plugins).thenReturn(emptyList())
      val newDocument = mock(Document::class.java)
      `when`(newDocument.id).thenReturn(DocumentId())

      `when`(
        documentRepository.saveAll(any2())
      ).thenAnswer { it.arguments[0] }
//      `when`(
//        documentService.findFirstByContentHashOrUrlAndRepositoryId(
//          any2(),
//          any2(),
//          any2(),
//        )
//      ).thenReturn(null)

      `when`(
        scrapeService.scrape(
          any(Source::class.java),
          any(LogCollector::class.java)
        )
      ).thenReturn(
        ScrapeOutput(
          outputs = listOf(
            ScrapeActionOutput(
              index = 0,
              fragment = FragmentOutput(
                fragmentName = "feed",
                fragments = emptyList(),
                items = listOf(
                  newJsonItem(url = "", title = "updated.title"),
                )
              )
            )
          ),
          time = 0
        )
      )

      repositoryHarvester.harvestRepository(repositoryId)

      // then
      verify(documentUseCase, times(1)).triggerPostReleaseEffects(any2(), any2())
    }

  @Test
  fun `updates for existing documents will be processed, if repository has no plugins`() =
    runTest(context = RequestContext(groupId = GroupId(), userId = randomUserId())) {
      val existing = mock(Document::class.java)
      `when`(
        documentUseCase.findFirstByContentHashOrUrlAndRepositoryId(
          any(String::class.java),
          any(String::class.java),
          any(RepositoryId::class.java)
        )
      ).thenReturn(
        existing
      )

      val updatedStartingAt = LocalDateTime.now().plusMinutes(5)
      `when`(
        scrapeService.scrape(
          any(Source::class.java),
          any(LogCollector::class.java)
        )
      ).thenReturn(
        ScrapeOutput(
          outputs = listOf(
            ScrapeActionOutput(
              index = 0,
              fragment = FragmentOutput(
                fragmentName = "feed",
                fragments = emptyList(),
                items = listOf(
                  newJsonItem(
                    url = "",
                    title = "updated.title",
                    text = "updated.text",
                    tags = listOf("up", "date", "ed"),
                    startingAt = updatedStartingAt
                  ),
                )
              )
            )
          ),
          time = 0
        )
      )

      repositoryHarvester.harvestRepository(repositoryId)

//    TODO        verify(existing).title = "updated.title"
//            verify(existing).text = "updated.text"
//            verify(existing).startingAt = updatedStartingAt
//            verify(documentService).saveAll(argThat<List<Document>> {
//                it.count() == 1 && it.first() == existing
//            })
    }

  @Test
  @Disabled
  fun `documents will inherit the plugins defined in repository`() =
    runTest(context = RequestContext(groupId = GroupId(), userId = randomUserId())) {
      TODO("implement")
    }

  @Test
  @Disabled
  fun `scrape will update the retrieval count`() =
    runTest(context = RequestContext(groupId = GroupId(), userId = randomUserId())) {
      TODO("implement")
    }

  @Test
  fun `will follow pagination links`() =
    runTest(context = RequestContext(groupId = GroupId(), userId = randomUserId())) {
      `when`(
        sourcePipelineJobRepository.existsBySourceIdAndUrl(
          any(SourceId::class.java),
          any(String::class.java)
        )
      ).thenReturn(false)

      `when`(
        scrapeService.scrape(
          any(Source::class.java),
          any(LogCollector::class.java)
        )
      ).thenReturn(
        ScrapeOutput(
          outputs = listOf(
            ScrapeActionOutput(
              index = 0,
              fragment = FragmentOutput(
                fragmentName = "feed",
                fragments = listOf(
                  ScrapeExtractFragment(
                    data = MimeData(
                      mimeType = MIME_URL,
                      data = "https://foo.bar/page/1"
                    ),
                    uniqueBy = ScrapeExtractFragmentPart.data
                  )
                ),
                items = listOf(newJsonItem(url = "", title = "1"))
              )
            )
          ),
          time = 0
        )
      )

      repositoryHarvester.harvestRepository(repositoryId)

      verify(sourcePipelineJobRepository).saveAll(argThat<List<SourcePipelineJob>> { it.count() == 1 })
    }

  private fun newJsonItem(
    url: String,
    title: String,
    text: String = "",
    latLng: JsonPoint? = null,
    startingAt: LocalDateTime? = null,
    tags: List<String>? = null
  ): JsonItem {
    val item = JsonItem()
    item.title = title
    item.url = url
    item.text = text
    item.tags = tags
    item.latLng = latLng
    item.startingAt = startingAt
    item.publishedAt = LocalDateTime.now()
    return item
  }
}
