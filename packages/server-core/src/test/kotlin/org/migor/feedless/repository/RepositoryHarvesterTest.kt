package org.migor.feedless.repository

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.migor.feedless.ResumableHarvestException
import org.migor.feedless.data.jpa.enums.ReleaseStatus
import org.migor.feedless.data.jpa.enums.Vertical
import org.migor.feedless.document.DocumentDAO
import org.migor.feedless.document.DocumentEntity
import org.migor.feedless.document.DocumentService
import org.migor.feedless.feed.parser.json.JsonItem
import org.migor.feedless.feed.parser.json.JsonPoint
import org.migor.feedless.generated.types.FeedlessPlugins
import org.migor.feedless.generated.types.MimeData
import org.migor.feedless.generated.types.PluginExecutionParamsInput
import org.migor.feedless.generated.types.ScrapeExtractFragment
import org.migor.feedless.message.MessageService
import org.migor.feedless.pipeline.DocumentPipelineJobDAO
import org.migor.feedless.pipeline.DocumentPipelineJobEntity
import org.migor.feedless.pipeline.FragmentOutput
import org.migor.feedless.pipeline.SourcePipelineJobDAO
import org.migor.feedless.pipeline.SourcePipelineJobEntity
import org.migor.feedless.scrape.LogCollector
import org.migor.feedless.scrape.ScrapeActionOutput
import org.migor.feedless.scrape.ScrapeOutput
import org.migor.feedless.scrape.ScrapeService
import org.migor.feedless.scrape.WebExtractService.Companion.MIME_URL
import org.migor.feedless.session.RequestContext
import org.migor.feedless.source.SourceDAO
import org.migor.feedless.source.SourceEntity
import org.migor.feedless.user.TelegramConnectionDAO
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness
import java.time.Duration
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RepositoryHarvesterTest {

  private lateinit var documentDAO: DocumentDAO
  private lateinit var sourceDAO: SourceDAO
  private lateinit var meterRegistry: MeterRegistry
  private lateinit var repositoryDAO: RepositoryDAO
  private lateinit var repositoryService: RepositoryService
  private lateinit var scrapeService: ScrapeService
  private lateinit var repositoryHarvester: RepositoryHarvester
  private val repositoryId = UUID.randomUUID()

  private lateinit var repository: RepositoryEntity
  private lateinit var source: SourceEntity
  private lateinit var sourcePipelineJobDAO: SourcePipelineJobDAO
  private lateinit var documentPipelineJobDAO: DocumentPipelineJobDAO

  @BeforeEach
  fun setUp() = runTest {
    documentDAO = mock(DocumentDAO::class.java)
    sourceDAO = mock(SourceDAO::class.java)
    meterRegistry = mock(MeterRegistry::class.java)
    repositoryDAO = mock(RepositoryDAO::class.java)
    repositoryService = mock(RepositoryService::class.java)
    scrapeService = mock(ScrapeService::class.java)
    repositoryHarvester = mock(RepositoryHarvester::class.java)
    sourcePipelineJobDAO = mock(SourcePipelineJobDAO::class.java)
    documentPipelineJobDAO = mock(DocumentPipelineJobDAO::class.java)

    repositoryHarvester = RepositoryHarvester(
      documentDAO,
      documentPipelineJobDAO,
      mock(HarvestDAO::class.java),
      sourcePipelineJobDAO,
      mock(TelegramConnectionDAO::class.java),
      sourceDAO,
      repositoryDAO,
      scrapeService,
      mock(DocumentService::class.java),
      meterRegistry,
      mock(MessageService::class.java),
      repositoryService
    )

    `when`(meterRegistry.counter(any(String::class.java), anyList())).thenReturn(mock(Counter::class.java))
    `when`(meterRegistry.counter(any(String::class.java))).thenReturn(mock(Counter::class.java))

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

    `when`(sourceDAO.findAllByRepositoryIdOrderByCreatedAtDesc(any(UUID::class.java))).thenReturn(mutableListOf(source))

    `when`(repositoryDAO.findById(eq(repositoryId))).thenReturn(Optional.of(repository))

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
        any(SourceEntity::class.java),
        any(LogCollector::class.java)
      )
    ).thenThrow(
      IllegalArgumentException("this is off")
    )
    `when`(source.errorsInSuccession).thenReturn(0)

    repositoryHarvester.handleRepository(repository.id)

    verify(scrapeService, times(1)).scrape(
      any(SourceEntity::class.java),
      any(LogCollector::class.java)
    )

    verify(source).disabled = false
    verify(source).errorsInSuccession = 1
    verify(source).lastErrorMessage = "this is off"
    verify(sourceDAO, times(1)).save(source)
  }

  @Test
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
    verify(sourceDAO, times(1)).save(source)
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
    verify(sourceDAO, times(1)).save(source)
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
      ResumableHarvestException("", Duration.ofMinutes(5))
    )

    // when
    repositoryHarvester.handleRepository(repository.id)

    // then
    assertThat(source.errorsInSuccession).isEqualTo(0)
    verify(scrapeService, times(1)).scrape(
      any(SourceEntity::class.java),
      any(LogCollector::class.java)
    )
    verify(sourceDAO, times(0)).save(source)
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
                  newJsonItem(url = "https://example.org/1", title = "1"),
                  newJsonItem(url = "https://example.org/1", title = "1"),
                  newJsonItem(url = "https://example.org/3", title = "3"),
                  newJsonItem(url = "https://example.org/4", title = "4"),
                )
              )
            )
          ),
          time = 0
        )
      )

      repositoryHarvester.handleRepository(repositoryId)

      verify(documentDAO).saveAll(argThat<Iterable<DocumentEntity>> { it.count() == 3 })
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

      verify(documentDAO).saveAll(argThat<Iterable<DocumentEntity>> { it.count() == 2 })
    }

  @Test
  fun `updates for existing documents will be ignored, if repository has plugins`() = runTest(context = RequestContext(userId = UUID.randomUUID())) {
    `when`(repository.plugins).thenReturn(listOf(mock(PluginExecution::class.java)))
    val existing = mock(DocumentEntity::class.java)
    `when`(
      documentDAO.findFirstByContentHashOrUrlAndRepositoryId(
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

    verify(documentDAO).saveAll(argThat<Iterable<DocumentEntity>> {
      it.count() == 0
    })
  }

  private fun createPlugin(): PluginExecution {
    return PluginExecution(FeedlessPlugins.org_feedless_fulltext.name, PluginExecutionParamsInput())
  }

  @Test
  fun `updates for existing documents will be processed, if repository has changed after existing has been created`() = runTest(context = RequestContext(userId = UUID.randomUUID())) {
    `when`(repository.plugins).thenReturn(listOf(createPlugin(), createPlugin()))
    val existing = mock(DocumentEntity::class.java)
    `when`(existing.id).thenReturn(UUID.randomUUID())
    `when`(
      documentDAO.findFirstByContentHashOrUrlAndRepositoryId(
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

    verify(documentPipelineJobDAO).deleteAllByDocumentIdIn(argThat {
      it.count() == 1
    })
    verify(documentPipelineJobDAO).saveAll(argThat<Iterable<DocumentPipelineJobEntity>> {
      it.count() == 2 // number of plugins
    })
    verify(existing).status = ReleaseStatus.unreleased
  }

  @Test
  fun `updates for existing documents will be processed, if repository has no plugins`() = runTest(context = RequestContext(userId = UUID.randomUUID())) {
    val existing = mock(DocumentEntity::class.java)
    `when`(
      documentDAO.findFirstByContentHashOrUrlAndRepositoryId(
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
                newJsonItem(url = "", title = "updated.title", text = "updated.text", tags = listOf("up", "date", "ed"), startingAt = updatedStartingAt),
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
    verify(documentDAO).saveAll(argThat<Iterable<DocumentEntity>> {
      it.count() == 1 && it.first() == existing
    })
  }

//  @Test
//  fun `documents will inherit the plugins defined in repository`() =
//    runTest(context = RequestContext(userId = UUID.randomUUID())) {
//      TODO()
//  }

  @Test
  fun `will follow pagination links`() = runTest(context = RequestContext(userId = UUID.randomUUID())) {
    `when`(
      sourcePipelineJobDAO.existsBySourceIdAndUrl(
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

    verify(sourcePipelineJobDAO).saveAll(argThat<Iterable<SourcePipelineJobEntity>> { it.count() == 1 })
  }

  private fun newJsonItem(url: String, title: String, text: String = "", latLng: JsonPoint? = null, startingAt: LocalDateTime? = null, tags: List<String>? = null): JsonItem {
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
