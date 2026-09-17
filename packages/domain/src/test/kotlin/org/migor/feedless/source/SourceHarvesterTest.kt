package org.migor.feedless.source

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions
import org.asynchttpclient.exception.TooManyConnectionsPerHostException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.migor.feedless.HostBlockedException
import org.migor.feedless.HostOverloadingException
import org.migor.feedless.Mother
import org.migor.feedless.ResumableHarvestException
import org.migor.feedless.Vertical
import org.migor.feedless.actions.FetchAction
import org.migor.feedless.actions.PluginExecutionJson
import org.migor.feedless.any
import org.migor.feedless.any2
import org.migor.feedless.anyList
import org.migor.feedless.argThat
import org.migor.feedless.capability.RequestContext
import org.migor.feedless.document.Document
import org.migor.feedless.document.DocumentId
import org.migor.feedless.document.DocumentRepository
import org.migor.feedless.document.DocumentUseCase
import org.migor.feedless.eq
import org.migor.feedless.feed.parser.json.JsonItem
import org.migor.feedless.feed.parser.json.JsonPoint
import org.migor.feedless.group.GroupId
import org.migor.feedless.harvest.Harvest
import org.migor.feedless.harvest.HarvestRepository
import org.migor.feedless.harvest.HarvestStatus
import org.migor.feedless.pipelineJob.DocumentPipelineJob
import org.migor.feedless.pipelineJob.DocumentPipelineJobRepository
import org.migor.feedless.pipelineJob.PluginExecution
import org.migor.feedless.pipelineJob.SourcePipelineJob
import org.migor.feedless.pipelineJob.SourcePipelineJobRepository
import org.migor.feedless.repository.Repository
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.repository.RepositoryRepository
import org.migor.feedless.repository.RepositoryUseCase
import org.migor.feedless.scrape.LogCollector
import org.migor.feedless.scrape.ScrapeMimeTypes
import org.migor.feedless.scrape.ScrapeResult
import org.migor.feedless.scrape.ScrapedData
import org.migor.feedless.scrape.ScrapedFragment
import org.migor.feedless.scrape.ScrapedFragmentOutput
import org.migor.feedless.scrape.ScrapedFragmentPart
import org.migor.feedless.scrape.Scraper
import org.migor.feedless.user.UserId
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SourceHarvesterTest {

  private lateinit var documentUseCase: DocumentUseCase
  private lateinit var sourceRepository: SourceRepository
  private lateinit var meterRegistry: MeterRegistry
  private lateinit var repositoryUseCase: RepositoryUseCase
  private lateinit var scraper: Scraper
  private lateinit var repositoryHarvester: SourceHarvester
  private lateinit var repositoryId: RepositoryId

  private lateinit var repository: Repository
  private lateinit var source: Source
  private lateinit var sourcePipelineJobRepository: SourcePipelineJobRepository
  private lateinit var documentPipelineJobRepository: DocumentPipelineJobRepository
  private lateinit var documentRepository: DocumentRepository
  private lateinit var repositoryRepository: RepositoryRepository
  private lateinit var harvestRepository: HarvestRepository

  @BeforeEach
  fun setUp() = runTest {
    repositoryId = Mother.randomRepositoryId()
    documentUseCase = Mockito.mock(DocumentUseCase::class.java)
    sourceRepository = Mockito.mock(SourceRepository::class.java)
    meterRegistry = Mockito.mock(MeterRegistry::class.java)
    repositoryUseCase = Mockito.mock(RepositoryUseCase::class.java)
    scraper = Mockito.mock(Scraper::class.java)
    sourcePipelineJobRepository = Mockito.mock(SourcePipelineJobRepository::class.java)
    documentPipelineJobRepository = Mockito.mock(DocumentPipelineJobRepository::class.java)
    documentRepository = Mockito.mock(DocumentRepository::class.java)
    repositoryRepository = Mockito.mock(RepositoryRepository::class.java)
    harvestRepository = Mockito.mock(HarvestRepository::class.java)

    repositoryHarvester = SourceHarvester(
      documentUseCase,
      documentRepository,
      documentPipelineJobRepository,
      sourcePipelineJobRepository,
      sourceRepository,
      scraper,
      meterRegistry,
      repositoryUseCase,
      repositoryRepository,
      harvestRepository,
    )

    Mockito.`when`(meterRegistry.counter(any2(), anyList())).thenReturn(Mockito.mock(Counter::class.java))
    Mockito.`when`(meterRegistry.counter(any2())).thenReturn(Mockito.mock(Counter::class.java))

    source = Mockito.mock(Source::class.java)
    Mockito.`when`(source.disabled).thenReturn(false)
    Mockito.`when`(source.id).thenReturn(SourceId())
    Mockito.`when`(source.repositoryId).thenReturn(repositoryId)
    Mockito.`when`(source.errorsInSuccession).thenReturn(0)

    repository = Mockito.mock(Repository::class.java)
    Mockito.`when`(repository.id).thenReturn(repositoryId)
    Mockito.`when`(repository.sourcesSyncCron).thenReturn("0 0 * * * *")
    Mockito.`when`(repository.groupId).thenReturn(GroupId())
    Mockito.`when`(repository.ownerId).thenReturn(UserId())
    Mockito.`when`(repository.product).thenReturn(Vertical.feedless)
    Mockito.`when`(repository.plugins).thenReturn(emptyList())

    // The source's real-run slot is free.
    Mockito.`when`(harvestRepository.startRun(any2(), any2())).thenAnswer {
      Harvest(
        sourceId = it.arguments[0] as SourceId,
        logs = "",
        startedAt = it.arguments[1] as LocalDateTime,
        finishedAt = null,
        status = HarvestStatus.RUNNING,
      )
    }

    Mockito.`when`(repositoryUseCase.findById(eq(repositoryId))).thenReturn(repository)
    Mockito.`when`(repositoryRepository.findById(eq(repositoryId))).thenReturn(repository)

    Mockito.`when`(
      repositoryUseCase.calculateScheduledNextAt(
        any(String::class.java), any(GroupId::class.java), any(
          LocalDateTime::class.java
        )
      )
    ).thenReturn(LocalDateTime.now())

    // No fresher reschedule since the source was claimed as due, unless a test overrides it.
    Mockito.`when`(sourceRepository.findNextHarvestAt(any2())).thenReturn(null)
  }

  @Test
  fun `given a late harvest, the offset timer records how late it ran`() = runTest {
    val registry = SimpleMeterRegistry()
    val harvester = SourceHarvester(
      documentUseCase,
      documentRepository,
      documentPipelineJobRepository,
      sourcePipelineJobRepository,
      sourceRepository,
      scraper,
      registry,
      repositoryUseCase,
      repositoryRepository,
      harvestRepository,
    )
    harvester.register()
    Mockito.`when`(source.nextHarvestAt).thenReturn(LocalDateTime.now().minusMinutes(10))

    harvester.harvestScheduled(source)

    val timer = registry.get("harvest.offset").timer()
    Assertions.assertThat(timer.count()).isEqualTo(1)
    Assertions.assertThat(timer.totalTime(TimeUnit.MINUTES)).isGreaterThanOrEqualTo(10.0)
  }

  @Test
  fun `given scrape fails will increment the error count`() = runTest {
    Mockito.`when`(
      scraper.scrape(
        any2(),
        any2()
      )
    ).thenThrow(
      IllegalArgumentException("this is off")
    )
    Mockito.`when`(source.errorsInSuccession).thenReturn(0)

    repositoryHarvester.harvestScheduled(source)

    Mockito.verify(scraper, Mockito.times(1)).scrape(
      any2(),
      any2()
    )

    // Incremented in the database, not saved from the (possibly stale) loaded source.
    Mockito.verify(sourceRepository, Mockito.times(1)).recordHarvestFailed(eq(source.id), eq("this is off"), any2())
    Mockito.verify(sourceRepository, Mockito.never()).save(any2())
  }

  @Test
  fun `given scrape fails without message, the harvest log names the exception type once`() = runTest {
    Mockito.`when`(scraper.scrape(any2(), any2())).thenThrow(IllegalArgumentException(""))

    repositoryHarvester.harvestScheduled(source)

    Mockito.verify(sourceRepository).recordHarvestFailed(eq(source.id), eq("IllegalArgumentException"), any2())
    Mockito.verify(harvestRepository).save(argThat {
      it.logs.contains("scrape failed IllegalArgumentException") && !it.logs.contains("scrape error")
    })
  }

  @Test
  fun `given scrape fails the harvest is recorded as errornous`() = runTest {
    Mockito.`when`(
      scraper.scrape(
        any2(),
        any2()
      )
    ).thenThrow(
      IllegalArgumentException("this is off")
    )

    repositoryHarvester.harvestScheduled(source)

    Mockito.verify(harvestRepository, Mockito.times(1)).save(argThat { it.errornous })
  }

  @Test
  fun `given scrape succeeds the harvest records itemsAdded`() =
    runTest(context = RequestContext(groupId = GroupId(), userId = Mother.randomUserId())) {
      Mockito.`when`(
        scraper.scrape(
          any(Source::class.java),
          any(LogCollector::class.java)
        )
      ).thenReturn(
        ScrapeResult(
          actionCount = 1,
          lastFragment = ScrapedFragmentOutput(
            fragments = emptyList(),
            items = listOf(
              newJsonItem(url = "https://example.org/1", title = "3"),
              newJsonItem(url = "https://example.org/3", title = "3"),
              newJsonItem(url = "https://example.org/4", title = "3"),
            )
          )
        )
      )

      repositoryHarvester.harvestScheduled(source)

      Mockito.verify(harvestRepository, Mockito.times(1)).save(argThat { !it.errornous && it.itemsAdded == 3 })
    }

  @Test
  @Disabled
  fun `given scrape works, errorCount will be reset`() = runTest {
    Mockito.`when`(source.errorsInSuccession).thenReturn(3)
    Mockito.`when`(
      scraper.scrape(
        any(Source::class.java),
        any(LogCollector::class.java)
      )
    ).thenReturn(
      ScrapeResult(actionCount = 0, lastFragment = null)
    )

    repositoryHarvester.harvestScheduled(source)

    Mockito.verify(sourceRepository, Mockito.times(1))
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
    Mockito.`when`(
      scraper.scrape(
        any(Source::class.java),
        any(LogCollector::class.java)
      )
    ).thenThrow(
      IllegalArgumentException("this is off")
    )
    Mockito.`when`(source.errorsInSuccession).thenReturn(4)

    repositoryHarvester.harvestScheduled(source)

    Mockito.verify(scraper, Mockito.times(1)).scrape(
      any(Source::class.java),
      any(LogCollector::class.java)
    )

    Mockito.verify(sourceRepository, Mockito.times(1))
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
    Assertions.assertThat(source.errorsInSuccession).isEqualTo(0)
    Mockito.`when`(
      scraper.scrape(
        any(Source::class.java),
        any(LogCollector::class.java)
      )
    ).thenThrow(
      ResumableHarvestException("they warned us about this", Duration.ofMinutes(5))
    )

    // when
    repositoryHarvester.harvestScheduled(source)

    // then
    Assertions.assertThat(source.errorsInSuccession).isEqualTo(0)
    Mockito.verify(scraper, Mockito.times(1)).scrape(
      any(Source::class.java),
      any(LogCollector::class.java)
    )

    Mockito.verify(sourceRepository, Mockito.times(1))
      .recordHarvestInterrupted(eq(source.id), eq("they warned us about this"), any2())
    Mockito.verify(sourceRepository, Mockito.never()).recordHarvestFailed(any2(), any2(), any2())
  }

  @Test
  fun `given documents feature a url, then urls will be used to deduplicate`() =
    runTest(context = RequestContext(groupId = GroupId(), userId = Mother.randomUserId())) {
      Mockito.`when`(
        scraper.scrape(
          any(Source::class.java),
          any(LogCollector::class.java)
        )
      ).thenReturn(
        ScrapeResult(
          actionCount = 1,
          lastFragment = ScrapedFragmentOutput(
            fragments = emptyList(),
            items = listOf(
              newJsonItem(url = "https://example.org/1", title = "3"),
              newJsonItem(url = "https://example.org/1", title = "3"),
              newJsonItem(url = "https://example.org/3", title = "3"),
              newJsonItem(url = "https://example.org/4", title = "3"),
            )
          )
        )
      )

      repositoryHarvester.harvestScheduled(source)

      Mockito.verify(documentRepository).saveAll(argThat { it.count() == 3 })
    }

  @Test
  fun `given documents feature fragments, the fragments will be persisted`() =
    runTest(context = RequestContext(groupId = GroupId(), userId = Mother.randomUserId())) {

      Mockito.`when`(
        documentUseCase.findFirstByContentHashOrUrlAndRepositoryId(
          any(String::class.java),
          any(String::class.java),
          any(RepositoryId::class.java)
        )
      ).thenReturn(null)
      Mockito.`when`(
        scraper.scrape(
          any(Source::class.java),
          any(LogCollector::class.java)
        )
      ).thenReturn(
        ScrapeResult(
          actionCount = 1,
          lastFragment = ScrapedFragmentOutput(
            fragments = listOf(
              ScrapedFragment(
                data = ScrapedData(mimeType = "image/png", data = "aGFsbG8K"),
                html = "html",
                text = "text",
                uniqueBy = ScrapedFragmentPart.html
              )
            ),
            items = emptyList(),
          )
        )
      )

      repositoryHarvester.harvestScheduled(source)

      Mockito.verify(documentRepository).saveAll(argThat { it.count() == 1 })
    }

  @Test
  fun `given no items but pagination links, the links will not be persisted as documents`() =
    runTest(context = RequestContext(groupId = GroupId(), userId = Mother.randomUserId())) {
      Mockito.`when`(
        scraper.scrape(
          any(Source::class.java),
          any(LogCollector::class.java)
        )
      ).thenReturn(
        ScrapeResult(
          actionCount = 1,
          lastFragment = ScrapedFragmentOutput(
            fragments = listOf(
              ScrapedFragment(
                data = ScrapedData(mimeType = ScrapeMimeTypes.MIME_URL, data = "https://foo.bar/page/2"),
                uniqueBy = ScrapedFragmentPart.data
              )
            ),
            items = emptyList(),
          )
        )
      )

      repositoryHarvester.harvestScheduled(source)

      Mockito.verify(documentRepository, Mockito.never()).saveAll(any2())
      Mockito.verify(sourceRepository, Mockito.never()).recordHarvestFailed(any2(), any2(), any2())
    }

  @Test
  fun `given documents feature no url, then titles will be used to deduplicate`() =
    runTest(context = RequestContext(groupId = GroupId(), userId = Mother.randomUserId())) {
      Mockito.`when`(
        scraper.scrape(
          any(Source::class.java),
          any(LogCollector::class.java)
        )
      ).thenReturn(
        ScrapeResult(
          actionCount = 1,
          lastFragment = ScrapedFragmentOutput(
            fragments = emptyList(),
            items = listOf(
              newJsonItem(url = "", title = "1"),
              newJsonItem(url = "", title = "1"),
              newJsonItem(url = "", title = "1"),
              newJsonItem(url = "", title = "4"),
            )
          )
        )
      )

      repositoryHarvester.harvestScheduled(source)

      Mockito.verify(documentRepository).saveAll(argThat { it.count() == 2 })
    }

  @Test
  fun `given documents feature no url, then the source url will be used as their url`() =
    runTest(context = RequestContext(groupId = GroupId(), userId = Mother.randomUserId())) {
      Mockito.`when`(source.actions).thenReturn(listOf(FetchAction(sourceId = SourceId(), url = "https://example.org/events")))
      Mockito.`when`(
        scraper.scrape(
          any(Source::class.java),
          any(LogCollector::class.java)
        )
      ).thenReturn(
        ScrapeResult(
          actionCount = 1,
          lastFragment = ScrapedFragmentOutput(
            fragments = emptyList(),
            items = listOf(
              newJsonItem(url = "", title = "1"),
              newJsonItem(url = "", title = "2"),
              newJsonItem(url = "https://example.org/events/3", title = "3"),
            )
          )
        )
      )

      repositoryHarvester.harvestScheduled(source)

      Mockito.verify(documentRepository).saveAll(argThat {
        it.map { document -> document.url } == listOf(
          "https://example.org/events",
          "https://example.org/events",
          "https://example.org/events/3"
        )
      })
    }

  @Test
  fun `updates for existing documents will be ignored, if repository has plugins`() =
    runTest(context = RequestContext(groupId = GroupId(), userId = Mother.randomUserId())) {
      Mockito.`when`(repository.plugins).thenReturn(listOf(Mockito.mock(PluginExecution::class.java)))
      val existing = Mockito.mock(Document::class.java)
      Mockito.`when`(
        documentUseCase.findFirstByContentHashOrUrlAndRepositoryId(
          any(String::class.java),
          any(String::class.java),
          any(RepositoryId::class.java)
        )
      ).thenReturn(
        existing
      )

      Mockito.`when`(
        scraper.scrape(
          any(Source::class.java),
          any(LogCollector::class.java)
        )
      ).thenReturn(
        ScrapeResult(
          actionCount = 1,
          lastFragment = ScrapedFragmentOutput(
            fragments = emptyList(),
            items = listOf(
              newJsonItem(url = "", title = "updated.title"),
            )
          )
        )
      )

      repositoryHarvester.harvestScheduled(source)

      Mockito.verify(documentRepository).saveAll(argThat {
        it.isEmpty()
      })
    }

  private fun createPlugin(): PluginExecution {
    return PluginExecution("org_feedless_fulltext", PluginExecutionJson())
  }

  @Test
  @Disabled("lastUpdateAt is polluted and cannot be used atm")
  fun `updates for existing documents will be processed, if repository has changed after existing has been created`() =
    runTest(context = RequestContext(groupId = GroupId(), userId = Mother.randomUserId())) {
      Mockito.`when`(repository.plugins).thenReturn(listOf(createPlugin(), createPlugin()))
      val existing = Mockito.mock(Document::class.java)
      Mockito.`when`(existing.id).thenReturn(DocumentId())
      Mockito.`when`(
        documentUseCase.findFirstByContentHashOrUrlAndRepositoryId(
          any(String::class.java),
          any(String::class.java),
          any(RepositoryId::class.java)
        )
      ).thenReturn(
        existing
      )

      val date = LocalDateTime.now()
      Mockito.`when`(repository.lastUpdatedAt).thenReturn(date)
      Mockito.`when`(existing.createdAt).thenReturn(date.minusMinutes(1))

      Mockito.`when`(
        scraper.scrape(
          any(Source::class.java),
          any(LogCollector::class.java)
        )
      ).thenReturn(
        ScrapeResult(
          actionCount = 1,
          lastFragment = ScrapedFragmentOutput(
            fragments = emptyList(),
            items = listOf(
              newJsonItem(url = "", title = "updated.title"),
            )
          )
        )
      )

      repositoryHarvester.harvestScheduled(source)

      Mockito.verify(documentPipelineJobRepository).deleteAllByDocumentIdIn(argThat {
        it.count() == 1
      })
      Mockito.verify(documentPipelineJobRepository).saveAll(argThat {
        it.count() == 2 // number of plugins
      })
//         TODO   verify(existing).status = ReleaseStatus.unreleased
    }

  @Test
  fun `released documents will trigger post release effects`() =
    runTest(context = RequestContext(groupId = GroupId(), userId = Mother.randomUserId())) {
      Mockito.`when`(repository.plugins).thenReturn(emptyList())
      val newDocument = Mockito.mock(Document::class.java)
      Mockito.`when`(newDocument.id).thenReturn(DocumentId())

      Mockito.`when`(
        documentRepository.saveAll(any2())
      ).thenAnswer { it.arguments[0] }
//      `when`(
//        documentService.findFirstByContentHashOrUrlAndRepositoryId(
//          any2(),
//          any2(),
//          any2(),
//        )
//      ).thenReturn(null)

      Mockito.`when`(
        scraper.scrape(
          any(Source::class.java),
          any(LogCollector::class.java)
        )
      ).thenReturn(
        ScrapeResult(
          actionCount = 1,
          lastFragment = ScrapedFragmentOutput(
            fragments = emptyList(),
            items = listOf(
              newJsonItem(url = "", title = "updated.title"),
            )
          )
        )
      )

      repositoryHarvester.harvestScheduled(source)

      // then
      Mockito.verify(documentUseCase, Mockito.times(1)).triggerPostReleaseEffects(any2(), any2())
    }

  @Test
  fun `updates for existing documents will be processed, if repository has no plugins`() =
    runTest(context = RequestContext(groupId = GroupId(), userId = Mother.randomUserId())) {
      val existing = Mockito.mock(Document::class.java)
      Mockito.`when`(
        documentUseCase.findFirstByContentHashOrUrlAndRepositoryId(
          any(String::class.java),
          any(String::class.java),
          any(RepositoryId::class.java)
        )
      ).thenReturn(
        existing
      )

      val updatedStartingAt = LocalDateTime.now().plusMinutes(5)
      Mockito.`when`(
        scraper.scrape(
          any(Source::class.java),
          any(LogCollector::class.java)
        )
      ).thenReturn(
        ScrapeResult(
          actionCount = 1,
          lastFragment = ScrapedFragmentOutput(
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
      )

      repositoryHarvester.harvestScheduled(source)

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
    runTest(context = RequestContext(groupId = GroupId(), userId = Mother.randomUserId())) {
      TODO("implement")
    }

  @Test
  fun `scrape will update the retrieval count, existing items included`() =
    runTest(context = RequestContext(groupId = GroupId(), userId = Mother.randomUserId())) {
      Mockito.`when`(repository.plugins).thenReturn(listOf(createPlugin()))
      Mockito.`when`(
        documentUseCase.findFirstByContentHashOrUrlAndRepositoryId(
          any(String::class.java),
          any(String::class.java),
          any(RepositoryId::class.java)
        )
      ).thenReturn(Mockito.mock(Document::class.java))
      Mockito.`when`(
        scraper.scrape(
          any(Source::class.java),
          any(LogCollector::class.java)
        )
      ).thenReturn(
        ScrapeResult(
          actionCount = 1,
          lastFragment = ScrapedFragmentOutput(
            fragments = emptyList(),
            items = listOf(
              newJsonItem(url = "https://example.org/1", title = "1"),
              newJsonItem(url = "https://example.org/2", title = "2"),
            )
          )
        )
      )

      repositoryHarvester.harvestScheduled(source)

      Mockito.verify(sourceRepository).recordHarvestSucceeded(eq(source.id), eq(2), any2())
      Mockito.verify(harvestRepository).save(argThat { it.itemsAdded == 0 })
    }

  @Test
  fun `given existing items and plugins, the harvest log names the skipped items and queues no plugins`() =
    runTest(context = RequestContext(groupId = GroupId(), userId = Mother.randomUserId())) {
      Mockito.`when`(repository.plugins).thenReturn(listOf(createPlugin()))
      Mockito.`when`(
        documentUseCase.findFirstByContentHashOrUrlAndRepositoryId(
          any(String::class.java),
          any(String::class.java),
          any(RepositoryId::class.java)
        )
      ).thenReturn(Mockito.mock(Document::class.java))
      Mockito.`when`(scraper.scrape(any(Source::class.java), any(LogCollector::class.java))).thenReturn(
        ScrapeResult(
          actionCount = 1,
          lastFragment = ScrapedFragmentOutput(
            fragments = emptyList(),
            items = listOf(newJsonItem(url = "https://example.org/1", title = "1"))
          )
        )
      )

      repositoryHarvester.harvestScheduled(source)

      Mockito.verify(harvestRepository).save(argThat {
        it.logs.contains("0 new, 1 existing (https://example.org/1)") &&
          it.logs.contains("no new items, not running plugins [org_feedless_fulltext]") &&
          !it.logs.contains("with [org_feedless_fulltext]")
      })
    }

  @Test
  fun `given new items and plugins, the harvest log says which plugins were queued`() =
    runTest(context = RequestContext(groupId = GroupId(), userId = Mother.randomUserId())) {
      Mockito.`when`(repository.plugins).thenReturn(listOf(createPlugin()))
      Mockito.`when`(scraper.scrape(any(Source::class.java), any(LogCollector::class.java))).thenReturn(
        ScrapeResult(
          actionCount = 1,
          lastFragment = ScrapedFragmentOutput(
            fragments = emptyList(),
            items = listOf(newJsonItem(url = "https://example.org/1", title = "1"))
          )
        )
      )

      repositoryHarvester.harvestScheduled(source)

      Mockito.verify(harvestRepository).save(argThat {
        it.logs.contains("queued 1 new items for [org_feedless_fulltext]") &&
          !it.logs.contains("queued for post-processing")
      })
      Mockito.verify(documentPipelineJobRepository).saveAll(argThat<List<DocumentPipelineJob>> { jobs ->
        jobs.size == 1 && jobs.all { it.harvestId != null }
      })
    }

  @Test
  fun `given the scrape yields nothing, the harvest is recorded as interrupted`() = runTest {
    Mockito.`when`(
      scraper.scrape(
        any(Source::class.java),
        any(LogCollector::class.java)
      )
    ).thenReturn(
      ScrapeResult(actionCount = 0, lastFragment = null)
    )

    repositoryHarvester.harvestScheduled(source)

    Mockito.verify(sourceRepository).recordHarvestInterrupted(eq(source.id), any2(), any2())
    Mockito.verify(sourceRepository, Mockito.never())
      .recordHarvestSucceeded(any2(), ArgumentMatchers.anyInt(), any2())
  }

  @Test
  fun `will follow pagination links`() =
    runTest(context = RequestContext(groupId = GroupId(), userId = Mother.randomUserId())) {
      Mockito.`when`(
        sourcePipelineJobRepository.existsBySourceIdAndUrl(
          any(SourceId::class.java),
          any(String::class.java)
        )
      ).thenReturn(false)

      Mockito.`when`(
        scraper.scrape(
          any(Source::class.java),
          any(LogCollector::class.java)
        )
      ).thenReturn(
        ScrapeResult(
          actionCount = 1,
          lastFragment = ScrapedFragmentOutput(
            fragments = listOf(
              ScrapedFragment(
                data = ScrapedData(
                  mimeType = ScrapeMimeTypes.MIME_URL,
                  data = "https://foo.bar/page/1"
                ),
                uniqueBy = ScrapedFragmentPart.data
              )
            ),
            items = listOf(newJsonItem(url = "", title = "1"))
          )
        )
      )

      repositoryHarvester.harvestScheduled(source)

      Mockito.verify(sourcePipelineJobRepository).saveAll(argThat<List<SourcePipelineJob>> { it.count() == 1 })
    }

  @Test
  fun `a successful harvest schedules the source at the cron's next date`() = runTest {
    val cronNext = LocalDateTime.now().plusHours(1)
    Mockito.`when`(repositoryUseCase.calculateScheduledNextAt(any2(), any2(), any2())).thenReturn(cronNext)
    Mockito.`when`(scraper.scrape(any2(), any2())).thenReturn(
      ScrapeResult(
        actionCount = 1,
        lastFragment = ScrapedFragmentOutput(fragments = emptyList(), items = emptyList())
      )
    )

    repositoryHarvester.harvestScheduled(source)

    Mockito.verify(sourceRepository).scheduleNextHarvest(eq(source.id), eq(cronNext))
  }

  @Test
  fun `a throttled harvest is delayed, not failed, and waits for the longer of cron and retry`() = runTest {
    val cronNext = LocalDateTime.now().plusMinutes(1)
    Mockito.`when`(repositoryUseCase.calculateScheduledNextAt(any2(), any2(), any2())).thenReturn(cronNext)
    Mockito.`when`(scraper.scrape(any2(), any2())).thenThrow(
      HostOverloadingException("throttled by www.bueron.ch (429), retry in 10m", Duration.ofMinutes(10))
    )

    repositoryHarvester.harvestScheduled(source)

    Mockito.verify(sourceRepository).recordHarvestInterrupted(
      eq(source.id),
      eq("throttled by www.bueron.ch (429), retry in 10m"),
      any2()
    )
    Mockito.verify(sourceRepository, Mockito.never()).recordHarvestFailed(any2(), any2(), any2())
    Mockito.verify(harvestRepository).save(argThat { !it.errornous && it.logs.contains("delayed until") })
    Mockito.verify(sourceRepository).scheduleNextHarvest(
      eq(source.id),
      argThat { it.isAfter(LocalDateTime.now().plusMinutes(9)) })
  }

  @Test
  fun `a blocked harvest is delayed by its ladder step`() = runTest {
    Mockito.`when`(repositoryUseCase.calculateScheduledNextAt(any2(), any2(), any2())).thenReturn(LocalDateTime.now())
    Mockito.`when`(scraper.scrape(any2(), any2())).thenThrow(
      HostBlockedException(
        "www.bueron.ch",
        403,
        2,
        Duration.ofMinutes(30)
      )
    )

    repositoryHarvester.harvestScheduled(source)

    Mockito.verify(sourceRepository).recordHarvestInterrupted(
      eq(source.id),
      eq("blocked by www.bueron.ch (403, strike 2), retry in 30m"),
      any2()
    )
    Mockito.verify(sourceRepository).scheduleNextHarvest(
      eq(source.id),
      argThat { it.isAfter(LocalDateTime.now().plusMinutes(29)) })
  }

  @Test
  fun `too many connections is delayed by 2 minutes`() = runTest {
    Mockito.`when`(repositoryUseCase.calculateScheduledNextAt(any2(), any2(), any2())).thenReturn(LocalDateTime.now())
    // thenThrow rejects it as an undeclared checked exception (it extends IOException); thenAnswer bypasses that check.
    Mockito.`when`(scraper.scrape(any2(), any2())).thenAnswer { throw TooManyConnectionsPerHostException(1) }

    repositoryHarvester.harvestScheduled(source)

    Mockito.verify(sourceRepository).recordHarvestInterrupted(eq(source.id), any2(), any2())
    Mockito.verify(sourceRepository).scheduleNextHarvest(
      eq(source.id),
      argThat { it.isAfter(LocalDateTime.now().plusSeconds(110)) })
  }

  @Test
  fun `a repository without cron is not scheduled`() = runTest {
    Mockito.`when`(repository.sourcesSyncCron).thenReturn("")
    Mockito.`when`(scraper.scrape(any2(), any2())).thenReturn(
      ScrapeResult(
        actionCount = 1,
        lastFragment = ScrapedFragmentOutput(fragments = emptyList(), items = emptyList())
      )
    )

    repositoryHarvester.harvestScheduled(source)

    Mockito.verify(sourceRepository, Mockito.never()).scheduleNextHarvest(any2(), any2())
  }

  @Test
  fun `a source whose run slot is taken is skipped`() = runTest {
    // doReturn, not when/thenReturn: the setUp answer casts its arguments, which throws when when() replays it with matcher placeholders.
    Mockito.doReturn(null).`when`(harvestRepository).startRun(any2(), any2())

    repositoryHarvester.harvestScheduled(source)

    Mockito.verify(scraper, Mockito.never()).scrape(any2(), any2())
  }

  @Test
  fun `a source rescheduled since it was claimed as due is skipped, without starting or scraping`() = runTest {
    Mockito.`when`(sourceRepository.findNextHarvestAt(eq(source.id))).thenReturn(LocalDateTime.now().plusMinutes(5))

    repositoryHarvester.harvestScheduled(source)

    Mockito.verify(harvestRepository, Mockito.never()).startRun(any2(), any2())
    Mockito.verify(scraper, Mockito.never()).scrape(any2(), any2())
  }

  @Test
  fun `scheduling falls back to now plus one hour when computing the next harvest fails`() = runTest {
    Mockito.`when`(
      repositoryUseCase.calculateScheduledNextAt(
        any2(),
        any2(),
        any2()
      )
    ).thenThrow(IllegalArgumentException("bad cron"))
    Mockito.`when`(scraper.scrape(any2(), any2())).thenReturn(
      ScrapeResult(
        actionCount = 1,
        lastFragment = ScrapedFragmentOutput(fragments = emptyList(), items = emptyList())
      )
    )

    repositoryHarvester.harvestScheduled(source)

    Mockito.verify(sourceRepository).scheduleNextHarvest(eq(source.id), argThat {
      it.isAfter(LocalDateTime.now().plusMinutes(59)) && it.isBefore(LocalDateTime.now().plusMinutes(61))
    })
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
