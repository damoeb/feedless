package org.migor.feedless.repository

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.migor.feedless.ResumableHarvestException
import org.migor.feedless.data.jpa.enums.ProductCategory
import org.migor.feedless.document.DocumentDAO
import org.migor.feedless.document.DocumentEntity
import org.migor.feedless.document.DocumentService
import org.migor.feedless.feed.parser.json.JsonItem
import org.migor.feedless.message.MessageService
import org.migor.feedless.pipeline.DocumentPipelineJobDAO
import org.migor.feedless.pipeline.FragmentOutput
import org.migor.feedless.pipeline.SourcePipelineJobDAO
import org.migor.feedless.scrape.LogCollector
import org.migor.feedless.scrape.ScrapeActionOutput
import org.migor.feedless.scrape.ScrapeOutput
import org.migor.feedless.scrape.ScrapeService
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

  @BeforeEach
  fun setUp() = runTest {
    documentDAO = mock(DocumentDAO::class.java)
    sourceDAO = mock(SourceDAO::class.java)
    meterRegistry = mock(MeterRegistry::class.java)
    repositoryDAO = mock(RepositoryDAO::class.java)
    repositoryService = mock(RepositoryService::class.java)
    scrapeService = mock(ScrapeService::class.java)
    repositoryHarvester = mock(RepositoryHarvester::class.java)

    repositoryHarvester = RepositoryHarvester(
      documentDAO,
      mock(DocumentPipelineJobDAO::class.java),
      mock(HarvestDAO::class.java),
      mock(SourcePipelineJobDAO::class.java),
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
    `when`(repository.product).thenReturn(ProductCategory.feedless)
    `when`(repository.plugins).thenReturn(emptyList())


    `when`(sourceDAO.findAllByRepositoryIdOrderByCreatedAtDesc(any(UUID::class.java))).thenReturn(mutableListOf(source))


    `when`(repositoryDAO.findById(eq(repositoryId))).thenReturn(Optional.of(repository))

    `when`(
      repositoryService.calculateScheduledNextAt(
        any(String::class.java), any(UUID::class.java), any(
          ProductCategory::class.java
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

//  @Test
//  fun `given scrape fails with resumable exception, error count is not changed`() = runTest {
//    TODO()
//  }

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
    `when`(
      scrapeService.scrape(
        any(SourceEntity::class.java),
        any(LogCollector::class.java)
      )
    ).thenThrow(
      ResumableHarvestException("", Duration.ofMinutes(5))
    )

    repositoryHarvester.handleRepository(repository.id)

    verify(scrapeService, times(1)).scrape(
      any(SourceEntity::class.java),
      any(LogCollector::class.java)
    )
    verify(sourceDAO, times(0)).save(source)
  }

  @Test
  fun `given documents feature a url, then urls will be used to deduplicate`() = runTest(context = RequestContext(userId = UUID.randomUUID())) {
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
                newJsonItem(url="https://example.org/1", title="1"),
                newJsonItem(url="https://example.org/1", title="1"),
                newJsonItem(url="https://example.org/3", title="3"),
                newJsonItem(url="https://example.org/4", title="4"),
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

  private fun newJsonItem(url: String, title: String): JsonItem {
    val item = JsonItem()
    item.title = title
    item.url = url
    item.text = ""
    item.publishedAt = LocalDateTime.now()
    return item
  }

  @Test

  fun `given documents feature no url, then titles will be used to deduplicate`() = runTest(context = RequestContext(userId = UUID.randomUUID())) {
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
                newJsonItem(url="", title="1"),
                newJsonItem(url="", title="1"),
                newJsonItem(url="", title="1"),
                newJsonItem(url="", title="4"),
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
}
