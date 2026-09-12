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
import org.migor.feedless.capability.RequestContext
import org.migor.feedless.eq
import org.migor.feedless.group.GroupId
import org.migor.feedless.pipeline.SourcePipelineService
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

class SourceUseCaseTest {

  private lateinit var sourceRepository: SourceRepository
  private lateinit var scrapeActionRepository: ScrapeActionRepository
  private lateinit var sourceUseCase: SourceUseCase
  private lateinit var repository: Repository
  private lateinit var repositoryId: RepositoryId
  private lateinit var repositoryRepository: RepositoryRepository
  private lateinit var groupId: GroupId

  @BeforeEach
  fun setUp() {
    sourceRepository = mock(SourceRepository::class.java)
    scrapeActionRepository = mock(ScrapeActionRepository::class.java)
    repositoryRepository = mock(RepositoryRepository::class.java)
    sourceUseCase = SourceUseCase(
      mock(SourcePipelineJobRepository::class.java),
      sourceRepository,
      mock(RepositoryHarvester::class.java),
      mock(PlanConstraintsService::class.java),
      scrapeActionRepository,
      repositoryRepository,
      mock(SourcePipelineService::class.java)
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
    val source = mock(Source::class.java)
    `when`(source.repositoryId).thenReturn(repositoryId)
    `when`(source.id).thenReturn(sourceId)
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
}
