package org.migor.feedless.pipelineJob

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.PostgreSQLExtension
import org.migor.feedless.actions.PluginExecutionJson
import org.migor.feedless.data.jpa.JpaDataTestApplication
import org.migor.feedless.document.Document
import org.migor.feedless.document.DocumentId
import org.migor.feedless.document.DocumentRepository
import org.migor.feedless.document.ReleaseStatus
import org.migor.feedless.group.Group
import org.migor.feedless.group.GroupRepository
import org.migor.feedless.harvest.Harvest
import org.migor.feedless.harvest.HarvestRepository
import org.migor.feedless.harvest.HarvestStatus
import org.migor.feedless.hostCooldown.HostCooldown
import org.migor.feedless.repository.Repository
import org.migor.feedless.repository.RepositoryRepository
import org.migor.feedless.source.Source
import org.migor.feedless.source.SourceRepository
import org.migor.feedless.user.User
import org.migor.feedless.user.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.LocalDateTime
import java.util.UUID

@SpringBootTest(classes = [JpaDataTestApplication::class])
@ExtendWith(PostgreSQLExtension::class)
@DirtiesContext
@ActiveProfiles(
  "test",
  "database",
  AppProfiles.repository,
  AppProfiles.source,
  AppProfiles.user,
  AppProfiles.document,
  AppProfiles.scrape,
  AppProfiles.plan,
  AppLayer.repository,
)
@Testcontainers
class DocumentPipelineJobRepositoryIntTest {

  @Autowired
  private lateinit var documentPipelineJobRepository: DocumentPipelineJobRepository

  @Autowired
  private lateinit var harvestRepository: HarvestRepository

  @Autowired
  private lateinit var documentRepository: DocumentRepository

  @Autowired
  private lateinit var sourceRepository: SourceRepository

  @Autowired
  private lateinit var repositoryRepository: RepositoryRepository

  @Autowired
  private lateinit var userRepository: UserRepository

  @Autowired
  private lateinit var groupRepository: GroupRepository

  @Autowired
  private lateinit var hostCooldown: HostCooldown

  @Autowired
  private lateinit var jdbcTemplate: JdbcTemplate

  private lateinit var repository: Repository
  private lateinit var source: Source

  @BeforeEach
  fun setUp() {
    jdbcTemplate.update("DELETE FROM t_host_cooldown")
    userRepository.deleteAll()
    val user = userRepository.save(User(email = "pipeline-job-${System.currentTimeMillis()}@test.com", lastLogin = LocalDateTime.now()))
    val group = groupRepository.save(Group(name = "pipeline-job-group", ownerId = user.id))
    repository = repositoryRepository.save(Repository(title = "pipeline-job-repo", ownerId = user.id, groupId = group.id))
    source = sourceRepository.save(Source(title = "pipeline-job-source", repositoryId = repository.id))
  }

  @Test
  fun `jobs wait until the harvest that queued them has completed`() {
    val harvest = harvestRepository.save(
      Harvest(sourceId = source.id, logs = "", startedAt = LocalDateTime.now(), finishedAt = null, status = HarvestStatus.RUNNING)
    )
    val job = queueJob(harvest)

    assertThat(documentPipelineJobRepository.findAllPendingBatched(LocalDateTime.now()).map { it.id }).doesNotContain(job.id)

    harvestRepository.save(harvest.copy(status = HarvestStatus.COMPLETED, finishedAt = LocalDateTime.now()))

    val pending = documentPipelineJobRepository.findAllPendingBatched(LocalDateTime.now())
    assertThat(pending.single { it.id == job.id }.harvestId).isEqualTo(harvest.id)
  }

  @Test
  fun `pruning the harvest keeps its jobs, which then run without a harvest log`() {
    val now = LocalDateTime.now()
    // Five completed harvests: the tail cleanup keeps the newest four and prunes the oldest.
    val oldest = completedHarvest(now.minusMinutes(5))
    (1..4).forEach { completedHarvest(now.minusMinutes(5L - it)) }
    val job = queueJob(oldest)

    harvestRepository.deleteAllTailingBySourceId()

    assertThat(harvestRepository.findById(oldest.id)).isNull()
    val pending = documentPipelineJobRepository.findAllPendingBatched(LocalDateTime.now())
    assertThat(pending.single { it.id == job.id }.harvestId).isNull()
  }

  @Test
  fun `jobs on a cooling host are not picked`() {
    val now = LocalDateTime.now()
    val job = queueJob(url = "https://Loop.example/article")
    hostCooldown.recordBlocked("loop.example", 403, now)

    assertThat(documentPipelineJobRepository.findAllPendingBatched(now).map { it.id }).doesNotContain(job.id)
  }

  @Test
  fun `jobs on a host with strikes come after jobs on clean hosts`() {
    val now = LocalDateTime.now()
    // The lowest document id, so only the strikes can push it behind the clean job.
    val struck = queueJob(url = "https://loop.example/article", documentId = DocumentId(UUID(0, 1)))
    val clean = queueJob(url = "https://clean.example/article")
    hostCooldown.recordBlocked("loop.example", 403, now.minusDays(2))

    val picked = documentPipelineJobRepository.findAllPendingBatched(now).map { it.id }

    assertThat(picked).containsSubsequence(clean.id, struck.id)
  }

  private fun completedHarvest(at: LocalDateTime): Harvest =
    harvestRepository.save(
      Harvest(sourceId = source.id, logs = "", startedAt = at, finishedAt = at, createdAt = at, status = HarvestStatus.COMPLETED)
    )

  private fun queueJob(
    harvest: Harvest? = null,
    url: String = "https://example.org/${System.nanoTime()}",
    documentId: DocumentId = DocumentId(),
  ): DocumentPipelineJob {
    val document = documentRepository.save(
      Document(
        id = documentId,
        url = url,
        title = "event",
        text = "",
        contentHash = "${System.nanoTime()}",
        status = ReleaseStatus.unreleased,
        repositoryId = repository.id,
        sourceId = source.id,
        publishedAt = LocalDateTime.now(),
      )
    )
    return documentPipelineJobRepository.save(
      DocumentPipelineJob(
        sequenceId = 0,
        documentId = document.id,
        pluginId = "org_feedless_fulltext",
        executorParams = PluginExecutionJson(),
        harvestId = harvest?.id,
      )
    )
  }
}
