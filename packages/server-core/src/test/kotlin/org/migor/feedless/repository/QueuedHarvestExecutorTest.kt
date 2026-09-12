package org.migor.feedless.repository

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.actions.FetchAction
import org.migor.feedless.any2
import org.migor.feedless.anyList
import org.migor.feedless.argThat
import org.migor.feedless.capability.RequestContext
import org.migor.feedless.document.DocumentRepository
import org.migor.feedless.document.DocumentUseCase
import org.migor.feedless.eq
import org.migor.feedless.feed.parser.json.JsonItem
import org.migor.feedless.group.GroupId
import org.migor.feedless.harvest.Harvest
import org.migor.feedless.harvest.HarvestRepository
import org.migor.feedless.harvest.HarvestStatus
import org.migor.feedless.http.api.model.HttpFetch
import org.migor.feedless.http.api.model.HttpGetRequest
import org.migor.feedless.http.api.model.ScrapeAction
import org.migor.feedless.http.api.model.ScrapeFlow
import org.migor.feedless.http.api.model.StringLiteralOrVariable
import org.migor.feedless.http.mapper.HttpScrapeFlowMapper
import org.migor.feedless.pipeline.FragmentOutput
import org.migor.feedless.pipelineJob.DocumentPipelineJobRepository
import org.migor.feedless.pipelineJob.SourcePipelineJobRepository
import org.migor.feedless.scrape.ScrapeActionOutput
import org.migor.feedless.scrape.ScrapeOutput
import org.migor.feedless.scrape.ScrapeService
import org.migor.feedless.scrape.ScraperAdapter
import org.migor.feedless.source.Source
import org.migor.feedless.source.SourceId
import org.migor.feedless.source.SourceRepository
import org.migor.feedless.user.UserId
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.dao.DataIntegrityViolationException
import java.time.LocalDateTime
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext

class QueuedHarvestExecutorTest {

  private lateinit var harvestRepository: HarvestRepository
  private lateinit var sourceRepository: SourceRepository
  private lateinit var repositoryRepository: RepositoryRepository
  private lateinit var documentRepository: DocumentRepository
  private lateinit var documentUseCase: DocumentUseCase
  private lateinit var scrapeService: ScrapeService
  private lateinit var executor: QueuedHarvestExecutor

  private val flowMapper = HttpScrapeFlowMapper()
  private val owner = UserId()
  private val repository = Repository(title = "repo", ownerId = owner, groupId = GroupId())
  private val source = Source(
    title = "source",
    repositoryId = repository.id,
    actions = listOf(FetchAction(sourceId = SourceId(), url = "https://example.org/saved")),
  )

  @BeforeEach
  fun setUp() {
    harvestRepository = mock(HarvestRepository::class.java)
    sourceRepository = mock(SourceRepository::class.java)
    repositoryRepository = mock(RepositoryRepository::class.java)
    documentRepository = mock(DocumentRepository::class.java)
    documentUseCase = mock(DocumentUseCase::class.java)
    scrapeService = mock(ScrapeService::class.java)
    val meterRegistry = mock(MeterRegistry::class.java)
    `when`(meterRegistry.counter(any2(), anyList())).thenReturn(mock(Counter::class.java))
    `when`(meterRegistry.counter(any2())).thenReturn(mock(Counter::class.java))

    // The real harvester and dry runner: what a run touches is the behaviour under test.
    val repositoryHarvester = RepositoryHarvester(
      documentUseCase,
      documentRepository,
      mock(DocumentPipelineJobRepository::class.java),
      mock(SourcePipelineJobRepository::class.java),
      sourceRepository,
      ScraperAdapter(scrapeService),
      meterRegistry,
      mock(RepositoryUseCase::class.java),
      repositoryRepository,
      harvestRepository,
    )
    executor = QueuedHarvestExecutor(
      harvestRepository,
      sourceRepository,
      repositoryRepository,
      repositoryHarvester,
      SourceDryRunner(scrapeService, harvestRepository),
      flowMapper,
    )

    `when`(sourceRepository.findByIdWithActions(eq(source.id))).thenReturn(source)
    `when`(repositoryRepository.findById(eq(repository.id))).thenReturn(repository)
  }

  @Test
  fun `a real run imports records and updates the source, under the owner's context`() = runTest {
    var scrapeContext: CoroutineContext? = null
    `when`(scrapeService.scrape(any2(), any2())).thenAnswer {
      // Mockito hides a suspend function's continuation from `arguments`; the raw ones keep it.
      scrapeContext = (it.rawArguments.last() as Continuation<*>).context
      scrapeOutput(item("First", "https://example.org/1"), item("Second", "https://example.org/2"))
    }

    val done = executor.execute(claimed(dryRun = false))

    val runAs = scrapeContext?.get(RequestContext)
    assertThat(runAs?.userId).isEqualTo(owner)
    assertThat(runAs?.groupId).isEqualTo(repository.groupId)
    verify(documentRepository).saveAll(argThat { it.count() == 2 })
    verify(sourceRepository).recordHarvestSucceeded(eq(source.id), eq(2), any2())
    assertThat(done.status).isEqualTo(HarvestStatus.COMPLETED)
    assertThat(done.errornous).isFalse()
    assertThat(done.itemsAdded).isEqualTo(2)
    assertThat(done.finishedAt).isNotNull()
    verify(harvestRepository).save(done)
  }

  @Test
  fun `a dry run scrapes the override flow and touches neither records nor the source`() = runTest {
    var scraped: Source? = null
    `when`(scrapeService.scrape(any2(), any2())).thenAnswer {
      scraped = it.arguments[0] as Source
      scrapeOutput(item("First", "https://example.org/1"), item("Second", "https://example.org/2"))
    }
    val flow = flowMapper.toStoredFlow(
      ScrapeFlow(
        sequence = listOf(
          ScrapeAction(fetch = HttpFetch(get = HttpGetRequest(url = StringLiteralOrVariable(literal = "https://example.org/fixed")))),
        ),
      )
    )

    val done = executor.execute(claimed(dryRun = true, flow = flow))

    assertThat((scraped!!.actions.single() as FetchAction).url).isEqualTo("https://example.org/fixed")
    verify(documentRepository, never()).saveAll(anyList())
    verifySourceUntouched()
    assertThat(done.status).isEqualTo(HarvestStatus.COMPLETED)
    // At least one item extracted: the dry run succeeded.
    assertThat(done.itemsAdded).isGreaterThanOrEqualTo(1).isEqualTo(2)
    assertThat(done.errornous).isFalse()
    assertThat(done.logs).contains(
      "dry run extracted 2 item(s)",
      "1. First  https://example.org/1",
      "2. Second  https://example.org/2",
    )
    verify(harvestRepository).save(done)
  }

  @Test
  fun `a dry run without an override flow scrapes the saved flow`() = runTest {
    var scraped: Source? = null
    `when`(scrapeService.scrape(any2(), any2())).thenAnswer {
      scraped = it.arguments[0] as Source
      scrapeOutput()
    }

    val done = executor.execute(claimed(dryRun = true))

    assertThat((scraped!!.actions.single() as FetchAction).url).isEqualTo("https://example.org/saved")
    // Extracting nothing is a failed dry run: a broken selector yields no items, not an exception.
    assertThat(done.status).isEqualTo(HarvestStatus.COMPLETED)
    assertThat(done.errornous).isTrue()
    assertThat(done.itemsAdded).isEqualTo(0)
    assertThat(done.logs).contains("dry run extracted no items", "dry run extracted 0 item(s)")
    verifySourceUntouched()
  }

  @Test
  fun `a failing dry run completes as errornous without touching the source`() = runTest {
    `when`(scrapeService.scrape(any2(), any2())).thenThrow(IllegalArgumentException("no such element"))

    val done = executor.execute(claimed(dryRun = true))

    assertThat(done.status).isEqualTo(HarvestStatus.COMPLETED)
    assertThat(done.errornous).isTrue()
    assertThat(done.logs).contains("scrape failed no such element")
    verifySourceUntouched()
  }

  @Test
  fun `an unexpected exception still completes the harvest, as errornous`() = runTest {
    `when`(sourceRepository.findByIdWithActions(eq(source.id))).thenThrow(IllegalStateException("database gone"))

    val done = executor.execute(claimed(dryRun = false))

    assertThat(done.status).isEqualTo(HarvestStatus.COMPLETED)
    assertThat(done.errornous).isTrue()
    assertThat(done.finishedAt).isNotNull()
    assertThat(done.logs).contains("harvest failed: database gone")
    verify(harvestRepository).save(done)
  }

  @Test
  fun `a harvest whose stored flow no longer maps completes as errornous`() = runTest {
    val done = executor.execute(claimed(dryRun = true, flow = """{"sequence":[{}]}"""))

    assertThat(done.status).isEqualTo(HarvestStatus.COMPLETED)
    assertThat(done.errornous).isTrue()
    assertThat(done.logs).contains("sets no action")
    verify(scrapeService, never()).scrape(any2(), any2())
  }

  @Test
  fun `a real run of a source disabled since it was queued completes as errornous without scraping`() = runTest {
    `when`(sourceRepository.findByIdWithActions(eq(source.id))).thenReturn(source.copy(disabled = true))

    val done = executor.execute(claimed(dryRun = false))

    assertThat(done.status).isEqualTo(HarvestStatus.COMPLETED)
    assertThat(done.errornous).isTrue()
    assertThat(done.logs).contains("source is disabled")
    verify(scrapeService, never()).scrape(any2(), any2())
  }

  @Test
  fun `each tick completes stale runs, then claims and runs queued harvests`() {
    val harvest = claimed(dryRun = true)
    `when`(harvestRepository.claimQueued(eq(QueuedHarvestExecutor.MAX_CONCURRENT_RUNS), any2())).thenReturn(listOf(harvest))
    runTest { `when`(scrapeService.scrape(any2(), any2())).thenReturn(scrapeOutput()) }
    val before = LocalDateTime.now()

    executor.executeQueuedHarvests()

    var cutoff: LocalDateTime? = null
    var message: String? = null
    verify(harvestRepository).completeStaleRunning(
      argThat { cutoff = it; true },
      any2(),
      argThat { message = it; true },
    )
    assertThat(cutoff).isBetween(before.minusMinutes(30), LocalDateTime.now().minusMinutes(30))
    assertThat(message).contains("harvest timed out: still running after 30 minutes")
    verify(harvestRepository).save(argThat { it.id == harvest.id && it.status == HarvestStatus.COMPLETED })
  }

  @Test
  fun `a claim the database refuses leaves every harvest queued and the tick quiet`() {
    `when`(harvestRepository.claimQueued(eq(QueuedHarvestExecutor.MAX_CONCURRENT_RUNS), any2()))
      .thenThrow(DataIntegrityViolationException("uq_harvest_one_running_real_run_per_source"))

    executor.executeQueuedHarvests()

    verify(harvestRepository, never()).save(any2())
    runTest { verify(scrapeService, never()).scrape(any2(), any2()) }
  }

  private fun verifySourceUntouched() {
    verify(sourceRepository, never()).save(any2())
    verify(sourceRepository, never()).recordHarvestSucceeded(any2(), anyInt(), any2())
    verify(sourceRepository, never()).recordHarvestFailed(any2(), any2(), any2())
    verify(sourceRepository, never()).recordHarvestInterrupted(any2(), any2(), any2())
  }

  private fun claimed(dryRun: Boolean, flow: String? = null) = Harvest(
    sourceId = source.id,
    logs = "",
    startedAt = LocalDateTime.now(),
    finishedAt = null,
    status = HarvestStatus.RUNNING,
    dryRun = dryRun,
    flow = flow,
  )

  private fun scrapeOutput(vararg items: JsonItem) = ScrapeOutput(
    outputs = listOf(
      ScrapeActionOutput(
        index = 0,
        fragment = FragmentOutput(fragmentName = "feed", fragments = emptyList(), items = items.toList()),
      )
    ),
    time = 0,
  )

  private fun item(title: String, url: String): JsonItem {
    val item = JsonItem()
    item.title = title
    item.url = url
    item.text = ""
    item.publishedAt = LocalDateTime.now()
    return item
  }
}
