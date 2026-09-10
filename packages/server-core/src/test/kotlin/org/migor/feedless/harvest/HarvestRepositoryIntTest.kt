package org.migor.feedless.harvest

import com.google.gson.JsonParser
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.PageableRequest
import org.migor.feedless.PostgreSQLExtension
import org.migor.feedless.data.jpa.harvest.HarvestDAO
import org.migor.feedless.group.Group
import org.migor.feedless.group.GroupRepository
import org.migor.feedless.repository.Repository
import org.migor.feedless.repository.RepositoryRepository
import org.migor.feedless.session.StatelessAuthService
import org.migor.feedless.source.Source
import org.migor.feedless.source.SourceId
import org.migor.feedless.source.SourceRepository
import org.migor.feedless.user.User
import org.migor.feedless.user.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.LocalDateTime
import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

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
class HarvestRepositoryIntTest {

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

  private lateinit var sourceA: Source
  private lateinit var sourceB: Source

  @BeforeEach
  fun setUp() {
    userRepository.deleteAll()

    val user = userRepository.save(
      User(
        email = "harvest-test-${System.currentTimeMillis()}@test.com",
        lastLogin = LocalDateTime.now(),
      )
    )
    val group = groupRepository.save(Group(name = "harvest-test-group", ownerId = user.id))
    val repository = repositoryRepository.save(
      Repository(
        title = "harvest-test-repo",
        ownerId = user.id,
        groupId = group.id,
      )
    )

    sourceA = sourceRepository.save(Source(title = "source-a", repositoryId = repository.id))
    sourceB = sourceRepository.save(Source(title = "source-b", repositoryId = repository.id))
  }

  private fun harvest(
    sourceId: SourceId,
    createdAt: LocalDateTime,
    status: HarvestStatus = HarvestStatus.COMPLETED,
    dryRun: Boolean = false,
  ): Harvest {
    return harvestRepository.save(
      Harvest(
        sourceId = sourceId,
        logs = "",
        startedAt = createdAt,
        finishedAt = createdAt,
        createdAt = createdAt,
        status = status,
        dryRun = dryRun,
      )
    )
  }

  @Test
  fun `flow is persisted and read back as an opaque JSON string`() {
    val flowJson = """{"override":true,"steps":["a","b"]}"""
    val saved = harvestRepository.save(
      Harvest(
        sourceId = sourceA.id,
        logs = "",
        startedAt = LocalDateTime.now(),
        finishedAt = null,
        dryRun = true,
        flow = flowJson,
      )
    )

    val reloaded = harvestDAO.findById(saved.id.uuid).get()

    // jsonb round-trips through Postgres, which re-serializes (key order, spacing) but preserves
    // the JSON value itself - T3 treats flow as opaque, so only structural equality is guaranteed.
    assertThat(JsonParser.parseString(reloaded.flow)).isEqualTo(JsonParser.parseString(flowJson))
  }

  @Test
  fun `claimQueued claims the oldest queued harvests and marks them running`() {
    harvestDAO.deleteAllInBatch()
    val now = LocalDateTime.now()
    val oldest = harvest(sourceA.id, now.minusMinutes(3), HarvestStatus.QUEUED)
    val middle = harvest(sourceB.id, now.minusMinutes(2), HarvestStatus.QUEUED, dryRun = true)
    val newest = harvest(sourceB.id, now.minusMinutes(1), HarvestStatus.QUEUED)
    // A running dry run does not hold sourceA's real-run slot (a running real one would).
    val running = harvest(sourceA.id, now.minusMinutes(10), HarvestStatus.RUNNING, dryRun = true)
    val completed = harvest(sourceA.id, now.minusMinutes(10), HarvestStatus.COMPLETED)
    val claimedAt = now.withNano(0)

    val claimed = harvestRepository.claimQueued(2, claimedAt)

    assertThat(claimed.map { it.id }).containsExactly(oldest.id, middle.id)
    assertThat(claimed).allSatisfy {
      assertThat(it.status).isEqualTo(HarvestStatus.RUNNING)
      assertThat(it.startedAt).isEqualTo(claimedAt)
    }
    assertThat(claimed.single { it.id == middle.id }.dryRun).isTrue()
    assertThat(statusOf(oldest, middle, newest, running, completed)).containsExactly(
      HarvestStatus.RUNNING, HarvestStatus.RUNNING, HarvestStatus.QUEUED, HarvestStatus.RUNNING, HarvestStatus.COMPLETED,
    )
    // The claimed rows are committed as running: nobody claims them again.
    assertThat(harvestRepository.claimQueued(10, now).map { it.id }).containsExactly(newest.id)
    assertThat(harvestRepository.claimQueued(10, now)).isEmpty()
  }

  @Test
  fun `concurrent claimers skip each other's locked rows and get disjoint harvests`() {
    harvestDAO.deleteAllInBatch()
    val now = LocalDateTime.now()
    // Dry runs: real runs of one source are claimed one at a time (OneRealHarvestPerSourceIntTest).
    val queued = (1..4).map { harvest(sourceA.id, now.minusMinutes(it.toLong()), HarvestStatus.QUEUED, dryRun = true) }
    val transactions = TransactionTemplate(transactionManager)
    val firstClaimed = CountDownLatch(1)
    val releaseFirst = CountDownLatch(1)
    val executor = Executors.newFixedThreadPool(2)
    try {
      // The first claimer holds its row locks — its transaction stays open until released.
      val first = executor.submit(Callable {
        transactions.execute {
          val claimed = harvestRepository.claimQueued(2, now)
          firstClaimed.countDown()
          releaseFirst.await(30, TimeUnit.SECONDS)
          claimed
        }!!
      })
      assertThat(firstClaimed.await(30, TimeUnit.SECONDS)).isTrue()

      // Without SKIP LOCKED the second claimer would block on those rows until the timeout.
      val second = executor.submit(Callable {
        transactions.execute { harvestRepository.claimQueued(10, now) }!!
      }).get(10, TimeUnit.SECONDS)

      releaseFirst.countDown()
      val firstIds = first.get(30, TimeUnit.SECONDS).map { it.id }
      val secondIds = second.map { it.id }

      assertThat(firstIds).hasSize(2).doesNotContainAnyElementsOf(secondIds)
      assertThat(firstIds + secondIds).containsExactlyInAnyOrderElementsOf(queued.map { it.id })
      assertThat(harvestRepository.claimQueued(10, now)).isEmpty()
    } finally {
      releaseFirst.countDown()
      executor.shutdownNow()
    }
  }

  @Test
  fun `completeStaleRunning fails only harvests running longer than the cutoff`() {
    harvestDAO.deleteAllInBatch()
    val now = LocalDateTime.now().withNano(0)
    val stale = harvestRepository.save(
      Harvest(
        sourceId = sourceA.id,
        logs = "scrape started",
        startedAt = now.minusMinutes(31),
        finishedAt = null,
        status = HarvestStatus.RUNNING,
      )
    )
    // Another source: sourceA's slot is held by the stale run.
    val fresh = harvest(sourceB.id, now.minusMinutes(5), HarvestStatus.RUNNING)
    val oldQueued = harvest(sourceA.id, now.minusHours(2), HarvestStatus.QUEUED)
    val oldCompleted = harvest(sourceA.id, now.minusHours(2), HarvestStatus.COMPLETED)

    val completed = harvestRepository.completeStaleRunning(now.minusMinutes(30), now, "timed out")

    assertThat(completed).isEqualTo(1)
    val failed = harvestRepository.findById(stale.id)!!
    assertThat(failed.status).isEqualTo(HarvestStatus.COMPLETED)
    assertThat(failed.errornous).isTrue()
    assertThat(failed.finishedAt).isEqualTo(now)
    assertThat(failed.logs).isEqualTo("scrape started\ntimed out")
    assertThat(statusOf(fresh, oldQueued, oldCompleted)).containsExactly(
      HarvestStatus.RUNNING, HarvestStatus.QUEUED, HarvestStatus.COMPLETED,
    )
    assertThat(harvestRepository.findById(oldCompleted.id)!!.errornous).isFalse()
  }

  private fun statusOf(vararg harvests: Harvest): List<HarvestStatus> =
    harvests.map { harvestRepository.findById(it.id)!!.status }

  @Test
  fun `findById returns the harvest`() {
    val saved = harvest(sourceA.id, LocalDateTime.now())

    val found = harvestRepository.findById(saved.id)

    assertThat(found?.id).isEqualTo(saved.id)
  }

  @Test
  fun `findById returns null when the harvest does not exist`() {
    val found = harvestRepository.findById(HarvestId())

    assertThat(found).isNull()
  }

  @Test
  fun `findAllBySourceId returns newest first and filters by dryRun`() {
    val now = LocalDateTime.now()
    val oldest = harvest(sourceA.id, now.minusMinutes(3))
    val middle = harvest(sourceA.id, now.minusMinutes(2))
    val newest = harvest(sourceA.id, now.minusMinutes(1))
    // a dry run harvest, more recent than all of the above, must not leak into dryRun=false results
    harvest(sourceA.id, now, dryRun = true)
    // a harvest for a different source must not leak in either
    harvest(sourceB.id, now)

    val result = harvestRepository.findAllBySourceId(sourceA.id, dryRun = false, PageableRequest(0, 10))

    assertThat(result.map { it.id }).containsExactly(newest.id, middle.id, oldest.id)
  }

  @Test
  fun `findAllBySourceId pages without skipping or repeating rows, mirroring listHarvests' ask-for-one-extra pattern`() {
    val now = LocalDateTime.now().withNano(0)
    val created = (1..5).map { n -> harvest(sourceA.id, now.minusMinutes(n.toLong())) }

    // Canonical, un-paginated order this walk must reproduce (createdAt desc).
    val all = harvestRepository.findAllBySourceId(sourceA.id, dryRun = false, PageableRequest(0, 10))
    assertThat(all.map { it.id }).containsExactlyElementsOf(created.map { it.id })

    // Mirrors HarvestService.findAllBySourceId: ask for one more than the page holds.
    var page = 0
    val returned = mutableListOf<HarvestId>()
    while (true) {
      val fetched = harvestRepository.findAllBySourceId(sourceA.id, dryRun = false, PageableRequest.withExtraForHasMore(page, 2))
      val hasMore = fetched.size > 2
      returned.addAll(fetched.take(2).map { it.id })
      if (!hasMore) break
      page++
    }

    assertThat(returned).containsExactlyElementsOf(all.map { it.id })
  }

  @Test
  fun `findAllBySourceId returns only dry runs when dryRun is true`() {
    val now = LocalDateTime.now()
    val dryRun = harvest(sourceA.id, now, dryRun = true)
    harvest(sourceA.id, now.minusMinutes(1), dryRun = false)

    val result = harvestRepository.findAllBySourceId(sourceA.id, dryRun = true, PageableRequest(0, 10))

    assertThat(result.map { it.id }).containsExactly(dryRun.id)
  }

  @Test
  @Transactional
  fun `deleteAllTailingBySourceId keeps the newest 4 completed harvests per source and dryRun`() {
    val now = LocalDateTime.now()
    val completedNonDryRun = (1..6).map { harvest(sourceA.id, now.minusMinutes(it.toLong()), HarvestStatus.COMPLETED, dryRun = false) }
    val completedDryRun = (1..6).map { harvest(sourceA.id, now.minusMinutes(it.toLong()), HarvestStatus.COMPLETED, dryRun = true) }
    val queued = harvest(sourceA.id, now.minusDays(30), HarvestStatus.QUEUED, dryRun = false)
    val running = harvest(sourceA.id, now.minusDays(30), HarvestStatus.RUNNING, dryRun = false)

    harvestRepository.deleteAllTailingBySourceId()

    val remainingNonDryRun = harvestDAO.findAll().filter { it.sourceId == sourceA.id.uuid && !it.dryRun }
    val remainingDryRun = harvestDAO.findAll().filter { it.sourceId == sourceA.id.uuid && it.dryRun }

    // 4 newest completed + the queued + the running harvest survive
    assertThat(remainingNonDryRun.map { it.id }).containsExactlyInAnyOrder(
      *completedNonDryRun.take(4).map { it.id.uuid }.toTypedArray(),
      queued.id.uuid,
      running.id.uuid,
    )
    assertThat(remainingDryRun.map { it.id }).containsExactlyInAnyOrderElementsOf(
      completedDryRun.take(4).map { it.id.uuid }
    )
  }

  @Test
  @Transactional
  fun `deleteAllDryRunByCreatedAtBefore only removes stale completed dry runs`() {
    val now = LocalDateTime.now()
    val staleCompleted = harvest(sourceA.id, now.minusDays(10), HarvestStatus.COMPLETED, dryRun = true)
    val freshCompleted = harvest(sourceA.id, now.minusDays(1), HarvestStatus.COMPLETED, dryRun = true)
    val staleRunning = harvest(sourceA.id, now.minusDays(10), HarvestStatus.RUNNING, dryRun = true)
    val staleQueued = harvest(sourceA.id, now.minusDays(10), HarvestStatus.QUEUED, dryRun = true)
    val staleNonDryRun = harvest(sourceA.id, now.minusDays(10), HarvestStatus.COMPLETED, dryRun = false)

    harvestRepository.deleteAllDryRunByCreatedAtBefore(now.minusDays(7))

    val remainingIds = harvestDAO.findAll().map { it.id }
    assertThat(remainingIds).doesNotContain(staleCompleted.id.uuid)
    assertThat(remainingIds).containsExactlyInAnyOrder(
      freshCompleted.id.uuid,
      staleRunning.id.uuid,
      staleQueued.id.uuid,
      staleNonDryRun.id.uuid,
    )
  }
}
