package org.migor.feedless.repository

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.migor.feedless.ResumableHarvestException
import org.migor.feedless.actions.PluginExecutionJsonEntity
import org.migor.feedless.data.jpa.enums.ReleaseStatus
import org.migor.feedless.data.jpa.enums.Vertical
import org.migor.feedless.document.DocumentEntity
import org.migor.feedless.document.DocumentService
import org.migor.feedless.feed.parser.json.JsonItem
import org.migor.feedless.feed.parser.json.JsonPoint
import org.migor.feedless.generated.types.FeedlessPlugins
import org.migor.feedless.generated.types.MimeData
import org.migor.feedless.generated.types.ScrapeExtractFragment
import org.migor.feedless.pipeline.DocumentPipelineJobEntity
import org.migor.feedless.pipeline.DocumentPipelineService
import org.migor.feedless.pipeline.FragmentOutput
import org.migor.feedless.pipeline.SourcePipelineJobEntity
import org.migor.feedless.pipeline.SourcePipelineService
import org.migor.feedless.scrape.LogCollector
import org.migor.feedless.scrape.ScrapeActionOutput
import org.migor.feedless.scrape.ScrapeOutput
import org.migor.feedless.scrape.ScrapeService
import org.migor.feedless.scrape.WebExtractService.Companion.MIME_URL
import org.migor.feedless.session.RequestContext
import org.migor.feedless.source.SourceEntity
import org.migor.feedless.source.SourceService
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness
import org.springframework.data.domain.Pageable
import java.time.Duration
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RepositoryHarvesterTest {

  private lateinit var documentService: DocumentService
  private lateinit var sourceService: SourceService
  private lateinit var meterRegistry: MeterRegistry
  private lateinit var repositoryService: RepositoryService
  private lateinit var scrapeService: ScrapeService
  private lateinit var repositoryHarvester: RepositoryHarvester
  private lateinit var repositoryId: UUID

  private lateinit var repository: RepositoryEntity
  private lateinit var source: SourceEntity
  private lateinit var sourcePipelineService: SourcePipelineService
  private lateinit var documentPipelineService: DocumentPipelineService

  @BeforeEach
  fun setUp() = runTest {
    repositoryId = UUID.randomUUID()
    documentService = mock(DocumentService::class.java)
    sourceService = mock(SourceService::class.java)
    meterRegistry = mock(MeterRegistry::class.java)
    repositoryService = mock(RepositoryService::class.java)
    scrapeService = mock(ScrapeService::class.java)
    sourcePipelineService = mock(SourcePipelineService::class.java)
    documentPipelineService = mock(DocumentPipelineService::class.java)

    repositoryHarvester = RepositoryHarvester(
      documentService,
      documentPipelineService,
      sourcePipelineService,
      sourceService,
      scrapeService,
      meterRegistry,
      repositoryService,
    )

    `when`(meterRegistry.counter(any2(), anyList())).thenReturn(mock(Counter::class.java))
    `when`(meterRegistry.counter(any2())).thenReturn(mock(Counter::class.java))

    source = mock(SourceEntity::class.java)
    `when`(source.disabled).thenReturn(false)
    `when`(source.id).thenReturn(UUID.randomUUID())
    `when`(source.repositoryId).thenReturn(repositoryId)
    `when`(source.errorsInSuccession).thenReturn(0)

    repository = mock(RepositoryEntity::class.java)
    `when`(repository.id).thenReturn(repositoryId)
    `when`(repository.sourcesSyncCron).thenReturn("")
    `when`(repository.ownerId).thenReturn(UUID.randomUUID())
    `when`(repository.product).thenReturn(Vertical.feedless)
    `when`(repository.plugins).thenReturn(emptyList())

    `when`(sourceService.findAllByRepositoryIdFiltered(any2(), any2(), eq(null), eq(null))).thenAnswer {
      if ((it.arguments[1] as Pageable).pageNumber == 0) {
        mutableListOf(source)
      } else {
        emptyList<SourceEntity>()
      }
    }

    `when`(repositoryService.findById(eq(repositoryId))).thenReturn(Optional.of(repository))

    `when`(
      repositoryService.calculateScheduledNextAt(
        any(String::class.java), any(UUID::class.java), any(
          Vertical::class.java
        ), any(
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

    repositoryHarvester.handleRepository(repository.id)

    verify(scrapeService, times(1)).scrape(
      any2(),
      any2()
    )

    verify(source).disabled = false
    verify(source).errorsInSuccession = 1
    verify(source).lastErrorMessage = "this is off"
    verify(sourceService, times(1)).save(source)
  }

  @Test
  @Disabled
  fun `given scrape works, errorCount will be reset`() = runTest {
    `when`(source.errorsInSuccession).thenReturn(3)
    `when`(
      scrapeService.scrape(
        any(SourceEntity::class.java),
        any(LogCollector::class.java)
      )
    ).thenReturn(
      ScrapeOutput(outputs = emptyList(), time = 0)
    )

    repositoryHarvester.handleRepository(repository.id)

    verify(source).errorsInSuccession = 0
    verify(source).lastErrorMessage = null
    verify(sourceService, times(1)).save(source)
  }

  @Test
  fun `given scrape fails will disable source once error-count threshold is met`() = runTest {
    `when`(
      scrapeService.scrape(
        any(SourceEntity::class.java),
        any(LogCollector::class.java)
      )
    ).thenThrow(
      IllegalArgumentException("this is off")
    )
    `when`(source.errorsInSuccession).thenReturn(4)

    repositoryHarvester.handleRepository(repository.id)

    verify(scrapeService, times(1)).scrape(
      any(SourceEntity::class.java),
      any(LogCollector::class.java)
    )

    verify(source).disabled = true
    verify(source).errorsInSuccession = 5
    verify(source).lastErrorMessage = "this is off"
    verify(sourceService, times(1)).save(source)
  }

  @Test
  fun `given scrape fails recoverable will not flag the source errornous`() = runTest {
    // given
    assertThat(source.errorsInSuccession).isEqualTo(0)
    `when`(
      scrapeService.scrape(
        any(SourceEntity::class.java),
        any(LogCollector::class.java)
      )
    ).thenThrow(
      ResumableHarvestException("they warned us about this", Duration.ofMinutes(5))
    )

    // when
    repositoryHarvester.handleRepository(repository.id)

    // then
    assertThat(source.errorsInSuccession).isEqualTo(0)
    verify(scrapeService, times(1)).scrape(
      any(SourceEntity::class.java),
      any(LogCollector::class.java)
    )

    verify(source).errorsInSuccession = 0
    verify(source).lastErrorMessage = "they warned us about this"
    verify(sourceService, times(1)).save(source)
  }

  @Test
  fun `given documents feature a url, then urls will be used to deduplicate`() =
    runTest(context = RequestContext(userId = UUID.randomUUID())) {
      `when`(
        scrapeService.scrape(
          any(SourceEntity::class.java),
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

      repositoryHarvester.handleRepository(repositoryId)

      verify(documentService).saveAll(argThat<List<DocumentEntity>> { it.count() == 3 })
    }

  @Test
  fun `given documents feature no url, then titles will be used to deduplicate`() =
    runTest(context = RequestContext(userId = UUID.randomUUID())) {
      `when`(
        scrapeService.scrape(
          any(SourceEntity::class.java),
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

      repositoryHarvester.handleRepository(repositoryId)

      verify(documentService).saveAll(argThat<List<DocumentEntity>> { it.count() == 2 })
    }

  @Test
  fun `updates for existing documents will be ignored, if repository has plugins`() =
    runTest(context = RequestContext(userId = UUID.randomUUID())) {
      `when`(repository.plugins).thenReturn(listOf(mock(PluginExecution::class.java)))
      val existing = mock(DocumentEntity::class.java)
      `when`(
        documentService.findFirstByContentHashOrUrlAndRepositoryId(
          any(String::class.java),
          any(String::class.java),
          any(UUID::class.java)
        )
      ).thenReturn(
        existing
      )

      `when`(
        scrapeService.scrape(
          any(SourceEntity::class.java),
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

      repositoryHarvester.handleRepository(repositoryId)

      verify(documentService).saveAll(argThat<List<DocumentEntity>> {
        it.count() == 0
      })
    }

  private fun createPlugin(): PluginExecution {
    return PluginExecution(FeedlessPlugins.org_feedless_fulltext.name, PluginExecutionJsonEntity())
  }

  @Test
  @Disabled("lastUpdateAt is polluted and cannot be used atm")
  fun `updates for existing documents will be processed, if repository has changed after existing has been created`() =
    runTest(context = RequestContext(userId = UUID.randomUUID())) {
      `when`(repository.plugins).thenReturn(listOf(createPlugin(), createPlugin()))
      val existing = mock(DocumentEntity::class.java)
      `when`(existing.id).thenReturn(UUID.randomUUID())
      `when`(
        documentService.findFirstByContentHashOrUrlAndRepositoryId(
          any(String::class.java),
          any(String::class.java),
          any(UUID::class.java)
        )
      ).thenReturn(
        existing
      )

      val date = LocalDateTime.now()
      `when`(repository.lastUpdatedAt).thenReturn(date)
      `when`(existing.createdAt).thenReturn(date.minusMinutes(1))

      `when`(
        scrapeService.scrape(
          any(SourceEntity::class.java),
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

      repositoryHarvester.handleRepository(repositoryId)

      verify(documentPipelineService).deleteAllByDocumentIdIn(argThat {
        it.count() == 1
      })
      verify(documentPipelineService).saveAll(argThat<List<DocumentPipelineJobEntity>> {
        it.count() == 2 // number of plugins
      })
      verify(existing).status = ReleaseStatus.unreleased
    }

  @Test
  fun `released documents will trigger post release effects`() =
    runTest(context = RequestContext(userId = UUID.randomUUID())) {
      `when`(repository.plugins).thenReturn(emptyList())
      val newDocument = mock(DocumentEntity::class.java)
      `when`(newDocument.id).thenReturn(UUID.randomUUID())

      `when`(
        documentService.saveAll(any2())
      ).thenAnswer{ it.arguments[0] }
//      `when`(
//        documentService.findFirstByContentHashOrUrlAndRepositoryId(
//          any2(),
//          any2(),
//          any2(),
//        )
//      ).thenReturn(null)

      `when`(
        scrapeService.scrape(
          any(SourceEntity::class.java),
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

      repositoryHarvester.handleRepository(repositoryId)

      // then
      verify(documentService, times(1)).triggerPostReleaseEffects(any2(), any2())
    }

  @Test
  fun `updates for existing documents will be processed, if repository has no plugins`() =
    runTest(context = RequestContext(userId = UUID.randomUUID())) {
      val existing = mock(DocumentEntity::class.java)
      `when`(
        documentService.findFirstByContentHashOrUrlAndRepositoryId(
          any(String::class.java),
          any(String::class.java),
          any(UUID::class.java)
        )
      ).thenReturn(
        existing
      )

      val updatedStartingAt = LocalDateTime.now().plusMinutes(5)
      `when`(
        scrapeService.scrape(
          any(SourceEntity::class.java),
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

      repositoryHarvester.handleRepository(repositoryId)

      verify(existing).title = "updated.title"
      verify(existing).text = "updated.text"
      verify(existing).startingAt = updatedStartingAt
      verify(documentService).saveAll(argThat<List<DocumentEntity>> {
        it.count() == 1 && it.first() == existing
      })
    }

  @Test
  @Disabled
  fun `documents will inherit the plugins defined in repository`() =
    runTest(context = RequestContext(userId = UUID.randomUUID())) {
      TODO()
    }

  @Test
  @Disabled
  fun `scrape will update the retrieval count`() =
    runTest(context = RequestContext(userId = UUID.randomUUID())) {
      TODO()
    }

  @Test
  fun `will follow pagination links`() = runTest(context = RequestContext(userId = UUID.randomUUID())) {
    `when`(
      sourcePipelineService.existsBySourceIdAndUrl(
        any(UUID::class.java),
        any(String::class.java)
      )
    ).thenReturn(false)

    `when`(
      scrapeService.scrape(
        any(SourceEntity::class.java),
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
                  )
                )
              ),
              items = listOf(newJsonItem(url = "", title = "1"))
            )
          )
        ),
        time = 0
      )
    )

    repositoryHarvester.handleRepository(repositoryId)

    verify(sourcePipelineService).saveAll(argThat<List<SourcePipelineJobEntity>> { it.count() == 1 })
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
