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
import org.springframework.transaction.annotation.Transactional
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.LocalDateTime

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
