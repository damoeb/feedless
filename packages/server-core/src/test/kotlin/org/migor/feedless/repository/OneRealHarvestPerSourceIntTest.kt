package org.migor.feedless.repository

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.PostgreSQLExtension
import org.migor.feedless.actions.FetchAction
import org.migor.feedless.any2
import org.migor.feedless.anyList
import org.migor.feedless.argThat
import org.migor.feedless.data.jpa.harvest.HarvestDAO
import org.migor.feedless.document.DocumentRepository
import org.migor.feedless.document.DocumentUseCase
import org.migor.feedless.group.Group
import org.migor.feedless.group.GroupRepository
import org.migor.feedless.harvest.Harvest
import org.migor.feedless.harvest.HarvestId
import org.migor.feedless.harvest.HarvestRepository
import org.migor.feedless.harvest.HarvestStatus
import org.migor.feedless.http.mapper.HttpScrapeFlowMapper
import org.migor.feedless.pipelineJob.DocumentPipelineJobRepository
import org.migor.feedless.pipelineJob.SourcePipelineJobRepository
import org.migor.feedless.scrape.ScrapeService
import org.migor.feedless.scrape.ScraperAdapter
import org.migor.feedless.session.StatelessAuthService
import org.migor.feedless.source.Source
import org.migor.feedless.source.SourceId
import org.migor.feedless.source.SourceRepository
import org.migor.feedless.user.User
import org.migor.feedless.user.UserRepository
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.LocalDateTime
import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit.SECONDS
import javax.sql.DataSource

/**
 * At most one real harvest per source, enforced by the database since scheduler and API may be separate processes.
 * The overlap tests hold one transaction open until the other session blocks on it, so the overlap is real.
 */
@SpringBootTest
@ExtendWith(PostgreSQLExtension::class)
@DirtiesContext
@ActiveProfiles(
  "test",
  "database",
  AppProfiles.repository,
  AppProfiles.source,
  AppProfiles.user,
  AppLayer.repository,
)
@MockitoBean(
  types = [
    StatelessAuthService::class,
  ]
)
@Testcontainers
class OneRealHarvestPerSourceIntTest {

  @Autowired
  private lateinit var harvestRepository: HarvestRepository

  @Autowired
  private lateinit var harvestDAO: HarvestDAO

  @Autowired
  private lateinit var sourceRepository: SourceRepository

  @Autowired
  private lateinit var repositoryRepository: RepositoryRepository

  @Autowired
  private lateinit var userRepository: UserRepository

  @Autowired
  private lateinit var groupRepository: GroupRepository

  @Autowired
  private lateinit var transactionManager: PlatformTransactionManager

  @Autowired
  private lateinit var dataSource: DataSource

  private lateinit var scrapeService: ScrapeService
  private lateinit var harvester: RepositoryHarvester
  private lateinit var executor: QueuedHarvestExecutor
  private lateinit var repository: Repository
  private lateinit var source: Source

  private val pool: ExecutorService = Executors.newFixedThreadPool(2)
  private val release = CountDownLatch(1)

  @BeforeEach
  fun setUp() {
    harvestDAO.deleteAllInBatch()
    userRepository.deleteAll()

    val user = userRepository.save(User(email = "one-harvest-${System.currentTimeMillis()}@test.com", lastLogin = LocalDateTime.now()))
    val group = groupRepository.save(Group(name = "one-harvest-group", ownerId = user.id))
    repository = repositoryRepository.save(
      Repository(title = "one-harvest-repo", ownerId = user.id, groupId = group.id, sourcesSyncCron = "0 0 * * * *")
    )
    source = createSource("busy")

    scrapeService = mock(ScrapeService::class.java)
    val meterRegistry = mock(MeterRegistry::class.java)
    `when`(meterRegistry.counter(any2(), anyList())).thenReturn(mock(Counter::class.java))
    `when`(meterRegistry.counter(any2())).thenReturn(mock(Counter::class.java))
    val repositoryUseCase = mock(RepositoryUseCase::class.java)
    runBlocking {
      `when`(scrapeService.scrape(any2(), any2())).thenThrow(IllegalArgumentException("broken selector"))
      `when`(repositoryUseCase.calculateScheduledNextAt(any2(), any2(), any2())).thenReturn(LocalDateTime.now().plusHours(1))
    }

    harvester = RepositoryHarvester(
      mock(DocumentUseCase::class.java),
      mock(DocumentRepository::class.java),
      mock(DocumentPipelineJobRepository::class.java),
      mock(SourcePipelineJobRepository::class.java),
      sourceRepository,
      ScraperAdapter(scrapeService),
      meterRegistry,
      repositoryUseCase,
      repositoryRepository,
      harvestRepository,
    )
    executor = QueuedHarvestExecutor(
      harvestRepository,
      sourceRepository,
      repositoryRepository,
      harvester,
      SourceDryRunner(scrapeService, harvestRepository),
      HttpScrapeFlowMapper(),
    )
  }

  @AfterEach
  fun tearDown() {
    release.countDown()
    pool.shutdownNow()
  }

  @Test
  fun `the database refuses a second running real harvest of a source, but not a running dry run`() {
    harvestRepository.save(harvest(HarvestStatus.RUNNING))

    assertThatThrownBy { harvestRepository.save(harvest(HarvestStatus.RUNNING)) }
      .isInstanceOf(DataIntegrityViolationException::class.java)
    harvestRepository.save(harvest(HarvestStatus.RUNNING, dryRun = true))
    harvestRepository.save(harvest(HarvestStatus.RUNNING, dryRun = true))
  }

  @Test
  fun `the scheduled loop skips a source whose real harvest is running and moves on to the next`() = runBlocking<Unit> {
    val running = harvestRepository.save(harvest(HarvestStatus.RUNNING))
    val idle = createSource("idle")

    harvester.harvestRepository(repository.id)

    verify(scrapeService, never()).scrape(argThat { it.id == source.id }, any2())
    verify(scrapeService).scrape(argThat { it.id == idle.id }, any2())
    assertThat(realHarvestsOf(source)).containsExactly(running.id to HarvestStatus.RUNNING)
    assertThat(sourceRepository.findById(source.id)!!.errorsInSuccession).isEqualTo(0)
    assertThat(sourceRepository.findById(idle.id)!!.errorsInSuccession).isEqualTo(1)
    // The scheduled run recorded its harvest as running first, then completed it.
    assertThat(realHarvestsOf(idle).map { it.second }).containsExactly(HarvestStatus.COMPLETED)
  }

  @Test
  fun `the queued executor leaves a real run queued while its source runs, then claims it on a later tick`() {
    val running = harvestRepository.save(harvest(HarvestStatus.RUNNING))
    val queued = harvestRepository.save(harvest(HarvestStatus.QUEUED))
    val queuedDryRun = harvestRepository.save(harvest(HarvestStatus.QUEUED, dryRun = true))

    executor.executeQueuedHarvests()

    assertThat(statusOf(queued)).isEqualTo(HarvestStatus.QUEUED)
    // Dry runs are unaffected by the running real harvest.
    assertThat(statusOf(queuedDryRun)).isEqualTo(HarvestStatus.COMPLETED)
    assertThat(sourceRepository.findById(source.id)!!.errorsInSuccession).isEqualTo(0)

    harvestRepository.save(running.copy(status = HarvestStatus.COMPLETED, finishedAt = LocalDateTime.now()))
    executor.executeQueuedHarvests()

    val done = harvestRepository.findById(queued.id)!!
    assertThat(done.status).isEqualTo(HarvestStatus.COMPLETED)
    assertThat(done.errornous).isTrue()
    assertThat(done.logs).contains("broken selector")
    assertThat(sourceRepository.findById(source.id)!!.errorsInSuccession).isEqualTo(1)
  }

  @Test
  fun `two sequential failed harvests leave errorsInSuccession at 2`() = runBlocking<Unit> {
    repeat(2) {
      harvester.harvestSource(sourceRepository.findByIdWithActions(source.id)!!, harvest(HarvestStatus.RUNNING))
    }

    assertThat(sourceRepository.findById(source.id)!!.errorsInSuccession).isEqualTo(2)
  }

  @Test
  fun `two overlapping failed harvests, both loaded before either finished, leave errorsInSuccession at 2`() = runBlocking<Unit> {
    // Both runs load the source at 0 errors.
    val loadedByFirst = sourceRepository.findByIdWithActions(source.id)!!
    val loadedBySecond = sourceRepository.findByIdWithActions(source.id)!!

    harvester.harvestSource(loadedByFirst, harvest(HarvestStatus.RUNNING))
    harvester.harvestSource(loadedBySecond, harvest(HarvestStatus.RUNNING))

    val reloaded = sourceRepository.findById(source.id)!!
    assertThat(reloaded.errorsInSuccession).isEqualTo(2)
    assertThat(reloaded.lastErrorMessage).isEqualTo("broken selector")
    assertThat(reloaded.lastRefreshedAt).isNotNull()
  }

  @Test
  fun `startRun gives a source's slot to one caller, and a running dry run neither takes nor blocks it`() {
    harvestRepository.save(harvest(HarvestStatus.RUNNING, dryRun = true))

    val first = harvestRepository.startRun(source.id, LocalDateTime.now())
    val second = harvestRepository.startRun(source.id, LocalDateTime.now())

    assertThat(first).isNotNull()
    assertThat(first!!.status).isEqualTo(HarvestStatus.RUNNING)
    assertThat(first.dryRun).isFalse()
    assertThat(second).isNull()
    assertThat(realHarvestsOf(source)).containsExactly(first.id to HarvestStatus.RUNNING)
    harvestRepository.save(harvest(HarvestStatus.RUNNING, dryRun = true))
  }

  @Test
  fun `two concurrent starts of one source, the first not yet committed, run exactly one harvest`() {
    val first = holdTransactionOpenAfter { harvestRepository.startRun(source.id, LocalDateTime.now()) }
    // Blocks on the first's uncommitted row in the unique index rather than racing it.
    val second = pool.submit(Callable { harvestRepository.startRun(source.id, LocalDateTime.now()) })
    awaitSessionWaitingForLock()
    release.countDown()

    assertThat(first.get(30, SECONDS)).isNotNull()
    assertThat(second.get(30, SECONDS)).isNull()
    assertThat(realHarvestsOf(source)).hasSize(1)
  }

  @Test
  fun `two failure updates overlapping at the SQL level both count`() {
    val first = holdTransactionOpenAfter { sourceRepository.recordHarvestFailed(source.id, "first", LocalDateTime.now()) }
    // Blocks on the first's row lock, then re-reads the committed row before incrementing.
    val second = pool.submit(Callable { sourceRepository.recordHarvestFailed(source.id, "second", LocalDateTime.now()) })
    awaitSessionWaitingForLock()
    release.countDown()
    first.get(30, SECONDS)
    second.get(30, SECONDS)

    val reloaded = sourceRepository.findById(source.id)!!
    assertThat(reloaded.errorsInSuccession).isEqualTo(2)
    assertThat(reloaded.lastErrorMessage).isEqualTo("second")
  }

  @Test
  fun `the atomic updates reset the count on success and on a passing failure, and fit the message`() {
    val now = LocalDateTime.now().withNano(0)
    sourceRepository.recordHarvestFailed(source.id, "x".repeat(300), now)
    assertThat(sourceRepository.findById(source.id)!!.lastErrorMessage).hasSize(255).endsWith("...")

    sourceRepository.recordHarvestSucceeded(source.id, 3, now)
    sourceRepository.findById(source.id)!!.let {
      assertThat(it.errorsInSuccession).isEqualTo(0)
      assertThat(it.lastErrorMessage).isNull()
      assertThat(it.lastRecordsRetrieved).isEqualTo(3)
      assertThat(it.lastRefreshedAt).isEqualTo(now)
    }

    sourceRepository.recordHarvestFailed(source.id, "broken", now)
    sourceRepository.recordHarvestInterrupted(source.id, "rate limited", now)
    sourceRepository.findById(source.id)!!.let {
      assertThat(it.errorsInSuccession).isEqualTo(0)
      assertThat(it.lastErrorMessage).isEqualTo("rate limited")
      assertThat(it.title).isEqualTo("busy")
    }
  }

  @Test
  fun `the stale sweep frees the source's slot`() {
    val now = LocalDateTime.now()
    harvestRepository.save(harvest(HarvestStatus.RUNNING, startedAt = now.minusMinutes(31)))
    assertThat(harvestRepository.startRun(source.id, now)).isNull()

    assertThat(harvestRepository.completeStaleRunning(now.minusMinutes(30), now, "timed out")).isEqualTo(1)

    assertThat(harvestRepository.startRun(source.id, now)).isNotNull()
  }

  @Test
  fun `only the oldest queued real run of a source is claimed, and only once none of it runs`() {
    val now = LocalDateTime.now()
    val older = harvestRepository.save(harvest(HarvestStatus.QUEUED).copy(createdAt = now.minusMinutes(2)))
    val newer = harvestRepository.save(harvest(HarvestStatus.QUEUED).copy(createdAt = now.minusMinutes(1)))
    val dryRun = harvestRepository.save(harvest(HarvestStatus.QUEUED, dryRun = true).copy(createdAt = now))

    assertThat(harvestRepository.claimQueued(10, now).map { it.id }).containsExactly(older.id, dryRun.id)
    assertThat(harvestRepository.claimQueued(10, now)).isEmpty()
    assertThat(statusOf(newer)).isEqualTo(HarvestStatus.QUEUED)

    harvestRepository.save(harvestRepository.findById(older.id)!!.copy(status = HarvestStatus.COMPLETED))
    assertThat(harvestRepository.claimQueued(10, now).map { it.id }).containsExactly(newer.id)
  }

  @Test
  fun `a claim racing an uncommitted scheduled start is refused by the database and leaves its run queued`() {
    val queued = harvestRepository.save(harvest(HarvestStatus.QUEUED))
    // The scheduled loop holds the slot but has not committed, so the claim's SELECT cannot see it.
    val scheduled = holdTransactionOpenAfter { harvestRepository.startRun(source.id, LocalDateTime.now()) }
    val claim = pool.submit(Callable { harvestRepository.claimQueued(10, LocalDateTime.now()) })
    awaitSessionWaitingForLock()
    release.countDown()

    assertThat(scheduled.get(30, SECONDS)).isNotNull()
    assertThatThrownBy { claim.get(30, SECONDS) }.hasCauseInstanceOf(DataIntegrityViolationException::class.java)
    assertThat(statusOf(queued)).isEqualTo(HarvestStatus.QUEUED)
    assertThat(realHarvestsOf(source).count { it.second == HarvestStatus.RUNNING }).isEqualTo(1)
  }

  /** Runs [work] in a transaction on the pool and keeps it open until [release]. */
  private fun <T> holdTransactionOpenAfter(work: () -> T): java.util.concurrent.Future<T?> {
    val done = CountDownLatch(1)
    val future = pool.submit(Callable {
      TransactionTemplate(transactionManager).execute {
        work().also {
          done.countDown()
          release.await(30, SECONDS)
        }
      }
    })
    assertThat(done.await(30, SECONDS)).isTrue()
    return future
  }

  private fun awaitSessionWaitingForLock() {
    val jdbc = JdbcTemplate(dataSource)
    val deadline = System.currentTimeMillis() + 10_000
    while (jdbc.queryForObject("SELECT count(*) FROM pg_stat_activity WHERE wait_event_type = 'Lock'", Int::class.java) == 0) {
      check(System.currentTimeMillis() < deadline) { "no session started waiting for a lock" }
      Thread.sleep(20)
    }
  }

  private fun createSource(title: String): Source {
    val id = SourceId()
    return sourceRepository.save(
      Source(
        id = id,
        title = title,
        repositoryId = repository.id,
        actions = listOf(FetchAction(sourceId = id, pos = 0, url = "https://example.org/$title")),
      )
    )
  }

  private fun harvest(
    status: HarvestStatus,
    dryRun: Boolean = false,
    startedAt: LocalDateTime = LocalDateTime.now(),
  ) = Harvest(
    sourceId = source.id,
    logs = "",
    startedAt = startedAt,
    finishedAt = null,
    status = status,
    dryRun = dryRun,
  )

  private fun statusOf(harvest: Harvest): HarvestStatus = harvestRepository.findById(harvest.id)!!.status

  private fun realHarvestsOf(source: Source): List<Pair<HarvestId, HarvestStatus>> =
    harvestDAO.findAll().filter { it.sourceId == source.id.uuid && !it.dryRun }
      .map { HarvestId(it.id) to HarvestStatus.valueOf(it.status.uppercase()) }
}
