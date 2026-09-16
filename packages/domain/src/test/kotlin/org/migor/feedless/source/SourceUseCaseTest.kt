package org.migor.feedless.source

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.migor.feedless.NotFoundException
import org.migor.feedless.Mother.randomRepositoryId
import org.migor.feedless.actions.ExtractXpathAction
import org.migor.feedless.actions.FetchAction
import org.migor.feedless.actions.ScrapeAction
import org.migor.feedless.any2
import org.migor.feedless.argThat
import org.migor.feedless.capability.MdcKeys
import org.migor.feedless.capability.RequestContext
import org.migor.feedless.eq
import org.migor.feedless.group.GroupId
import org.migor.feedless.hostCooldown.HostCooldown
import org.migor.feedless.hostCooldown.HostCooldownState
import org.migor.feedless.pipeline.SourcePipelineService
import org.migor.feedless.pipelineJob.SourcePipelineJob
import org.migor.feedless.pipelineJob.SourcePipelineJobRepository
import org.migor.feedless.plan.PlanConstraintsService
import org.migor.feedless.repository.Repository
import org.migor.feedless.repository.RepositoryHarvester
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.repository.RepositoryRepository
import org.migor.feedless.repository.RepositorySourceUpdate
import org.migor.feedless.user.UserId
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.slf4j.MDC
import java.time.LocalDateTime
import java.util.Collections

class SourceUseCaseTest {

  private lateinit var sourceRepository: SourceRepository
  private lateinit var scrapeActionRepository: ScrapeActionRepository
  private lateinit var sourceUseCase: SourceUseCase
  private lateinit var repository: Repository
  private lateinit var repositoryId: RepositoryId
  private lateinit var repositoryRepository: RepositoryRepository
  private lateinit var planConstraintsService: PlanConstraintsService
  private lateinit var hostCooldown: HostCooldown
  private lateinit var groupId: GroupId

  @BeforeEach
  fun setUp() {
    sourceRepository = mock(SourceRepository::class.java)
    scrapeActionRepository = mock(ScrapeActionRepository::class.java)
    repositoryRepository = mock(RepositoryRepository::class.java)
    planConstraintsService = mock(PlanConstraintsService::class.java)
    hostCooldown = mock(HostCooldown::class.java)
    sourceUseCase = SourceUseCase(
      mock(SourcePipelineJobRepository::class.java),
      sourceRepository,
      mock(RepositoryHarvester::class.java),
      planConstraintsService,
      scrapeActionRepository,
      repositoryRepository,
      mock(SourcePipelineService::class.java),
      hostCooldown,
    )

    repository = mock(Repository::class.java)
    repositoryId = randomRepositoryId()
    `when`(repository.id).thenReturn(repositoryId)

    groupId = GroupId()
    `when`(repository.groupId).thenReturn(groupId)
    `when`(repositoryRepository.findById(repositoryId)).thenReturn(repository)
  }

  @Test
  fun createSources() = runTest(context = RequestContext(groupId = groupId, userId = UserId())) {

    // what SourceInput.toSource() yields for a single fetch without url
    val inputs = listOf(
      Source(
        title = "wef",
        tags = emptyArray(),
        actions = mutableListOf(FetchAction(sourceId = SourceId(), url = "")),
        repositoryId = RepositoryId(),
      )
    )
    sourceUseCase.createSources(inputs, repositoryId)

    verify(sourceRepository).saveAll(argThat<List<Source>> { it.size == 1 })
    verify(scrapeActionRepository).saveAll(argThat<List<ScrapeAction>> { it.size == 1 })
  }

  @Test
  fun updateSources() = runTest(context = RequestContext(groupId = groupId, userId = UserId())) {
    val sourceId = SourceId()
    // what ScrapeFlowInput.fromDto() yields for a fetch plus a selector-based extract
    val actions = mutableListOf<ScrapeAction>(
      FetchAction(sourceId = SourceId(), url = "https::foo.bar"),
      ExtractXpathAction(
        sourceId = SourceId(),
        fragmentName = "foo",
        xpath = "//bar",
        uniqueBy = ExtractEmit.text,
        emit = arrayOf(ExtractEmit.text, ExtractEmit.pixel),
      ),
    )
    val source = Source(id = sourceId, title = "events", repositoryId = repositoryId)
    `when`(sourceRepository.findById(eq(sourceId))).thenReturn(source)

    `when`(scrapeActionRepository.findAllBySourceId(eq(sourceId))).thenReturn(listOf(mock(FetchAction::class.java)))

    val updates = listOf(
      RepositorySourceUpdate(
        sourceId = sourceId,
        disabled = false,
        actions = actions,
      )
    )

    sourceUseCase.updateSources(repositoryId, updates)

    verify(scrapeActionRepository).deleteAll(any2())
    verify(scrapeActionRepository).saveAll(argThat { it.size == 2 })
    verify(scrapeActionRepository).saveAll(argThat {
      assertThat((it.get(1) as ExtractXpathAction).emit).isEqualTo(
        arrayOf(
          ExtractEmit.text,
          ExtractEmit.pixel
        )
      )
      true
    })
    verify(sourceRepository).saveAll(argThat { it.size == 1 })
  }

  @Test
  fun `updateSources clears the last harvest error when the flow changes`() =
    runTest(context = RequestContext(groupId = groupId, userId = UserId())) {
      val sourceId = SourceId()
      val source = Source(
        id = sourceId,
        title = "events",
        repositoryId = repositoryId,
        lastErrorMessage = "no items retrieved",
        errorsInSuccession = 2,
      )
      `when`(sourceRepository.findById(eq(sourceId))).thenReturn(source)

      sourceUseCase.updateSources(
        repositoryId,
        listOf(
          RepositorySourceUpdate(
            sourceId = sourceId,
            actions = listOf(FetchAction(sourceId = sourceId, url = "https://foo.bar")),
          )
        ),
      )

      verify(sourceRepository).saveAll(argThat {
        it.single().lastErrorMessage == null && it.single().errorsInSuccession == 0
      })
    }

  @Test
  fun deleteAllById() = runTest(context = RequestContext(groupId = groupId, userId = UserId())) {
    val sources = listOf(
      SourceId(),
      SourceId(),
    ).map {
      val source = mock(Source::class.java)
      `when`(source.id).thenReturn(it)
      source
    }
    val sourceIds = sources.map { it.id }
    `when`(sourceRepository.findAllByRepositoryIdAndIdIn(repositoryId, sources.map { it.id })).thenReturn(sources)

    sourceUseCase.deleteAllById(repositoryId, sourceIds)

    verify(sourceRepository).deleteAllById(argThat<List<SourceId>> { it.size == sources.size })
  }

  @Test
  fun `updateSources throws NotFound when source missing`() = runTest(context = RequestContext(groupId = groupId, userId = UserId())) {
    val sourceId = SourceId()
    `when`(sourceRepository.findById(eq(sourceId))).thenReturn(null)

    assertThrows<NotFoundException> {
      sourceUseCase.updateSources(
        repositoryId,
        listOf(RepositorySourceUpdate(sourceId = sourceId, title = "new")),
      )
    }
  }

  @Test
  fun `updateSources throws NotFound when source belongs to another repository`() = runTest(context = RequestContext(groupId = groupId, userId = UserId())) {
    val sourceId = SourceId()
    val otherRepositoryId = randomRepositoryId()
    val source = mock(Source::class.java)
    `when`(source.repositoryId).thenReturn(otherRepositoryId)
    `when`(sourceRepository.findById(eq(sourceId))).thenReturn(source)

    assertThrows<NotFoundException> {
      sourceUseCase.updateSources(
        repositoryId,
        listOf(RepositorySourceUpdate(sourceId = sourceId, title = "new")),
      )
    }
  }

  @Test
  fun `deleteAllById throws NotFound when no sources match`() = runTest(context = RequestContext(groupId = groupId, userId = UserId())) {
    val sourceId = SourceId()
    `when`(sourceRepository.findAllByRepositoryIdAndIdIn(repositoryId, listOf(sourceId))).thenReturn(emptyList())

    assertThrows<NotFoundException> {
      sourceUseCase.deleteAllById(repositoryId, listOf(sourceId))
    }
  }

  @Test
  fun `deleteAllById throws when group does not match`() = runTest(context = RequestContext(groupId = GroupId(), userId = UserId())) {
    val sourceId = SourceId()

    assertThrows<IllegalArgumentException> {
      sourceUseCase.deleteAllById(repositoryId, listOf(sourceId))
    }
  }

  @Test
  fun `scheduleNextHarvestOfRepository coerces each source individually`() =
    runTest(context = RequestContext(groupId = groupId, userId = UserId())) {
      val coolingHost = "cooling.example"
      val okHost = "ok.example"
      val sourceAId = SourceId()
      val sourceBId = SourceId()
      val sourceALastRefreshedAt = LocalDateTime.of(2024, 1, 1, 0, 0)
      val sourceBLastRefreshedAt = LocalDateTime.of(2024, 6, 1, 0, 0)
      // Both floors sit far in the future, so real "now" never wins the max() inside HarvestTimeLimits.
      val sourceAPlanFloor = LocalDateTime.of(2030, 1, 1, 0, 0)
      val sourceBPlanFloor = LocalDateTime.of(2029, 1, 1, 0, 0)
      val hostBlockedUntil = LocalDateTime.of(2031, 1, 1, 0, 0)

      val sourceA = Source(
        id = sourceAId,
        title = "a",
        repositoryId = repositoryId,
        lastRefreshedAt = sourceALastRefreshedAt,
        actions = listOf(FetchAction(sourceId = sourceAId, url = "https://$okHost/feed")),
      )
      val sourceB = Source(
        id = sourceBId,
        title = "b",
        repositoryId = repositoryId,
        lastRefreshedAt = sourceBLastRefreshedAt,
        actions = listOf(FetchAction(sourceId = sourceBId, url = "https://$coolingHost/feed")),
      )
      `when`(sourceRepository.findAllWithActionsByRepositoryId(repositoryId)).thenReturn(listOf(sourceA, sourceB))
      `when`(planConstraintsService.coerceMinScheduledNextAt(eq(sourceALastRefreshedAt), eq(sourceALastRefreshedAt), eq(groupId)))
        .thenReturn(sourceAPlanFloor)
      `when`(planConstraintsService.coerceMinScheduledNextAt(eq(sourceBLastRefreshedAt), eq(sourceBLastRefreshedAt), eq(groupId)))
        .thenReturn(sourceBPlanFloor)
      `when`(hostCooldown.find(okHost)).thenReturn(null)
      `when`(hostCooldown.find(coolingHost)).thenReturn(
        HostCooldownState(host = coolingHost, blockedUntil = hostBlockedUntil, strikes = 1, lastStatus = 429)
      )

      // blank cron: no cap, so each source's coerced time comes only from its own floor
      sourceUseCase.scheduleNextHarvestOfRepository(repositoryId, null, "", groupId)

      verify(sourceRepository).scheduleNextHarvest(sourceAId, sourceAPlanFloor)
      verify(sourceRepository).scheduleNextHarvest(sourceBId, hostBlockedUntil)
    }

  // processSourceJobs runs its own runBlocking, so it is exercised directly rather than through runTest.
  @Test
  fun `processSourceJobs looks up owners under the run's correlation id`() {
    MDC.clear()
    MDC.put(MdcKeys.CORR_ID, "outer")
    try {
      val sourceId = SourceId()
      val job = SourcePipelineJob(sequenceId = 1, url = "https://example.com", sourceId = sourceId)
      val jobRepository = mock(SourcePipelineJobRepository::class.java)
      `when`(jobRepository.findAllPendingBatched(any2())).thenReturn(listOf(job))
      val seen = Collections.synchronizedList(mutableListOf<String?>())
      `when`(repositoryRepository.findBySourceId(eq(sourceId))).thenAnswer {
        seen.add(MDC.get(MdcKeys.CORR_ID))
        repository
      }
      val useCase = SourceUseCase(
        jobRepository,
        sourceRepository,
        mock(RepositoryHarvester::class.java),
        mock(PlanConstraintsService::class.java),
        scrapeActionRepository,
        repositoryRepository,
        mock(SourcePipelineService::class.java),
        hostCooldown,
      )

      useCase.processSourceJobs()

      assertThat(seen).hasSize(1)
      assertThat(seen[0]).isNotNull().startsWith("outer")
    } finally {
      MDC.clear()
    }
  }
}
