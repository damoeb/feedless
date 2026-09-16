package org.migor.feedless.source

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.PostgreSQLExtension
import org.migor.feedless.actions.FetchAction
import org.migor.feedless.common.hostOf
import org.migor.feedless.data.jpa.JpaDataTestApplication
import org.migor.feedless.data.jpa.source.SourceHostSql
import org.migor.feedless.group.Group
import org.migor.feedless.group.GroupRepository
import org.migor.feedless.harvest.HarvestRepository
import org.migor.feedless.hostCooldown.HostCooldown
import org.migor.feedless.repository.Repository
import org.migor.feedless.repository.RepositoryRepository
import org.migor.feedless.user.User
import org.migor.feedless.user.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.Duration
import java.time.LocalDateTime

@SpringBootTest(classes = [JpaDataTestApplication::class])
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
@Testcontainers
class SourceDueForHarvestIntTest {

  @Autowired private lateinit var sourceRepository: SourceRepository
  @Autowired private lateinit var harvestRepository: HarvestRepository
  @Autowired private lateinit var hostCooldown: HostCooldown
  @Autowired private lateinit var jdbcTemplate: JdbcTemplate
  @Autowired private lateinit var repositoryRepository: RepositoryRepository
  @Autowired private lateinit var userRepository: UserRepository
  @Autowired private lateinit var groupRepository: GroupRepository

  private lateinit var repository: Repository

  private val now = LocalDateTime.now()

  @BeforeEach
  fun setUp() {
    jdbcTemplate.update("DELETE FROM t_host_cooldown")
    userRepository.deleteAll()

    val user = userRepository.save(
      User(
        email = "source-due-test-${System.currentTimeMillis()}@test.com",
        lastLogin = LocalDateTime.now(),
        hasAcceptedTerms = true,
      )
    )
    val group = groupRepository.save(Group(name = "source-due-test-group", ownerId = user.id))
    repository = repositoryRepository.save(
      Repository(
        title = "source-due-test-repo",
        ownerId = user.id,
        groupId = group.id,
        sourcesSyncCron = "0 0 * * * *",
      )
    )
  }

  private fun createSource(url: String): Source {
    val id = SourceId()
    return sourceRepository.save(
      Source(id = id, title = url, repositoryId = repository.id, actions = listOf(FetchAction(sourceId = id, pos = 0, url = url)))
    )
  }

  @Test
  fun `a never-scheduled and an overdue source are due, a future one is not`() {
    val never = createSource("https://a.example/1")
    val overdue = createSource("https://b.example/1")
    val future = createSource("https://c.example/1")
    sourceRepository.scheduleNextHarvest(overdue.id, now.minusMinutes(1))
    sourceRepository.scheduleNextHarvest(future.id, now.plusHours(1))

    assertThat(sourceRepository.findAllDueForHarvest(now, 50).map { it.id })
      .containsExactly(never.id, overdue.id)
  }

  @Test
  fun `due sources come with their actions`() {
    createSource("https://a.example/1")

    assertThat(sourceRepository.findAllDueForHarvest(now, 50).single().actions).isNotEmpty()
  }

  @Test
  fun `sources on a cooling host are not due`() {
    createSource("https://www.Bueron.ch/index.php?apid=1")
    hostCooldown.recordThrottled("www.bueron.ch", 429, Duration.ofMinutes(10), now)

    assertThat(sourceRepository.findAllDueForHarvest(now, 50)).isEmpty()
  }

  @Test
  fun `disabled sources and sources with a running harvest are not due`() {
    val disabled = createSource("https://a.example/1")
    sourceRepository.save(sourceRepository.findByIdWithActions(disabled.id)!!.copy(disabled = true))
    val running = createSource("https://b.example/1")
    harvestRepository.startRun(running.id, now)

    assertThat(sourceRepository.findAllDueForHarvest(now, 50)).isEmpty()
  }

  @Test
  fun `findNextHarvestAt reads the current column without loading the rest of the source`() {
    val source = createSource("https://a.example/1")

    assertThat(sourceRepository.findNextHarvestAt(source.id)).isNull()

    val at = now.plusHours(1).withNano(0)
    sourceRepository.scheduleNextHarvest(source.id, at)

    assertThat(sourceRepository.findNextHarvestAt(source.id)).isEqualTo(at)
  }

  @Test
  fun `saving a source never overwrites next_harvest_at`() {
    val source = createSource("https://a.example/1")
    val at = now.plusHours(1).withNano(0)
    sourceRepository.scheduleNextHarvest(source.id, at)

    val loaded = sourceRepository.findByIdWithActions(source.id)!!
    sourceRepository.saveAll(listOf(loaded.copy(title = "edited", nextHarvestAt = null)))

    assertThat(sourceRepository.findNextHarvestAt(source.id)).isEqualTo(at)
    assertThat(sourceRepository.findByIdWithActions(source.id)!!.title).isEqualTo("edited")

    val laterAt = now.plusHours(2).withNano(0)
    sourceRepository.scheduleNextHarvest(source.id, laterAt)

    assertThat(sourceRepository.findNextHarvestAt(source.id)).isEqualTo(laterAt)
  }

  @Test
  fun `touchLastUpdatedAt changes only that column`() {
    val at = now.plusMinutes(1).withNano(0)

    repositoryRepository.touchLastUpdatedAt(repository.id, at)

    val reloaded = repositoryRepository.findById(repository.id)!!
    assertThat(reloaded.lastUpdatedAt).isEqualTo(at)
    assertThat(reloaded.title).isEqualTo(repository.title)
    assertThat(reloaded.sourcesSyncCron).isEqualTo(repository.sourcesSyncCron)
  }

  @Test
  fun `a repository's nextHarvestAt is the earliest of its enabled sources`() {
    val a = createSource("https://a.example/1")
    val b = createSource("https://b.example/1")
    val early = now.plusMinutes(5).withNano(0)
    sourceRepository.scheduleNextHarvest(a.id, now.plusHours(1))
    sourceRepository.scheduleNextHarvest(b.id, early)

    assertThat(repositoryRepository.findById(a.repositoryId!!)!!.nextHarvestAt).isEqualTo(early)
  }

  @ParameterizedTest
  @ValueSource(
    strings = [
      "https://www.Bueron.ch/index.php?apid=1659677497",
      "http://example.org:8080/a",
      "https://user:pw@example.org/a",
      "example.org/path",
      "HTTPS://EXAMPLE.ORG",
    ]
  )
  fun `the SQL host expression matches hostOf`(url: String) {
    val sql = jdbcTemplate.queryForObject("SELECT ${SourceHostSql.EXPRESSION.replace("f.url", "?::text")}", String::class.java, url)

    assertThat(sql).isEqualTo(hostOf(url))
  }
}
