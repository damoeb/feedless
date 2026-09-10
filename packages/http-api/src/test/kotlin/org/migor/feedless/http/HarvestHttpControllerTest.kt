package org.migor.feedless.http

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.EntityVisibility
import org.migor.feedless.actions.FetchAction
import org.migor.feedless.group.GroupUseCasePort
import org.migor.feedless.harvest.Harvest
import org.migor.feedless.harvest.HarvestId
import org.migor.feedless.harvest.HarvestUseCasePort
import org.migor.feedless.http.mapper.HttpHarvestMapper
import org.migor.feedless.repository.Repository
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.repository.RepositoryUseCasePort
import org.migor.feedless.source.Source
import org.migor.feedless.source.SourceId
import org.migor.feedless.source.SourceRepository
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import java.time.LocalDateTime

@WebMvcTest(controllers = [HarvestHttpController::class])
@AutoConfigureMockMvc(addFilters = false)
@Import(HttpHarvestMapper::class, HttpApiExceptionHandler::class, RepositoryAccessGuard::class, RequestContextBridge::class)
@ActiveProfiles("test", AppLayer.api, AppProfiles.source, AppProfiles.repository, AppProfiles.user)
class HarvestHttpControllerTest {

  @Autowired
  private lateinit var mockMvc: MockMvc

  @MockitoBean
  private lateinit var harvestUseCase: HarvestUseCasePort

  @MockitoBean
  private lateinit var sourceRepository: SourceRepository

  @MockitoBean
  private lateinit var repositoryUseCase: RepositoryUseCasePort

  @MockitoBean
  private lateinit var groupUseCase: GroupUseCasePort

  private val access by lazy { RepositoryAccessFixture(repositoryUseCase, groupUseCase) }

  @Test
  fun `listHarvests returns items and omits logs by default`() = runTest {
    val repo = access.givenRepository()
    val source = givenSource(repo.id)
    val harvest = harvest(sourceId = source.id, logs = "very long harvest log output")
    whenever(harvestUseCase.findAllBySourceId(eq(source.id), eq(0), eq(21))).thenReturn(listOf(harvest))

    val result = mockMvc.getAs(access.owner, "${harvestsUrl(repo, source)}?page=0&pageSize=20")

    assertStatus(result, 200)
    val body = result.response.contentAsString
    assert(body.contains("\"ok\":true")) { body }
    assert(body.contains("\"itemsAdded\":3")) { body }
    assert(body.contains("\"logs\":\"\"")) { body }
    assert(body.contains("\"hasMore\":false")) { body }
    assert(!body.contains("very long harvest log output")) { body }
  }

  @Test
  fun `listHarvests includes logs when includeLogs is true`() = runTest {
    val repo = access.givenRepository()
    val source = givenSource(repo.id)
    val harvest = harvest(sourceId = source.id, logs = "full harvest log output")
    whenever(harvestUseCase.findAllBySourceId(eq(source.id), eq(0), eq(21))).thenReturn(listOf(harvest))

    val result = mockMvc.getAs(access.owner, "${harvestsUrl(repo, source)}?page=0&pageSize=20&includeLogs=true")

    assertStatus(result, 200)
    assert(result.response.contentAsString.contains("full harvest log output")) { result.response.contentAsString }
  }

  @Test
  fun `listHarvests sets hasMore when a further item exists`() = runTest {
    val repo = access.givenRepository()
    val source = givenSource(repo.id)
    // pageSize + 1 available: there really is a next page
    val harvests = List(3) { harvest(sourceId = source.id) }
    whenever(harvestUseCase.findAllBySourceId(eq(source.id), eq(0), eq(3))).thenReturn(harvests)

    val result = mockMvc.getAs(access.owner, "${harvestsUrl(repo, source)}?page=0&pageSize=2")

    assertStatus(result, 200)
    assert(result.response.contentAsString.contains("\"hasMore\":true")) { result.response.contentAsString }
  }

  @Test
  fun `listHarvests reports hasMore false when the last page is exactly full`() = runTest {
    val repo = access.givenRepository()
    val source = givenSource(repo.id)
    // Exactly pageSize available. The old `items.size == pageSize` rule claimed a next
    // page here and made every client fetch an empty one.
    val harvests = List(2) { harvest(sourceId = source.id) }
    whenever(harvestUseCase.findAllBySourceId(eq(source.id), eq(0), eq(3))).thenReturn(harvests)

    val result = mockMvc.getAs(access.owner, "${harvestsUrl(repo, source)}?page=0&pageSize=2")

    assertStatus(result, 200)
    assert(result.response.contentAsString.contains("\"hasMore\":false")) { result.response.contentAsString }
  }

  @Test
  fun `listHarvests answers a group member and a stranger on a public repository`() = runTest {
    val private = access.givenRepository()
    val public = access.givenRepository(EntityVisibility.isPublic)
    whenever(harvestUseCase.findAllBySourceId(any(), any(), any())).thenReturn(emptyList())

    assertStatus(mockMvc.getAs(access.member, harvestsUrl(private, givenSource(private.id))), 200)
    assertStatus(mockMvc.getAs(access.stranger, harvestsUrl(public, givenSource(public.id))), 200)
  }

  @Test
  fun `listHarvests answers a stranger on a private repository like a missing one`() = runTest {
    val private = access.givenRepository()

    assertNotFound(
      mockMvc.getAs(access.stranger, harvestsUrl(private, givenSource(private.id))),
      "repository ${private.id.uuid} not found",
    )
    verify(harvestUseCase, never()).findAllBySourceId(any(), any(), any())
  }

  @Test
  fun `listHarvests returns 404 when source missing`() = runTest {
    val repo = access.givenRepository()
    val sourceId = SourceId()
    whenever(sourceRepository.findByIdWithActions(eq(sourceId))).thenReturn(null)

    val result = mockMvc.getAs(access.owner, "/api/v1/repositories/${repo.id.uuid}/sources/${sourceId.uuid}/harvests")

    assertNotFound(result, "source ${sourceId.uuid} not found")
    verify(harvestUseCase, never()).findAllBySourceId(any(), any(), any())
  }

  @Test
  fun `listHarvests returns 404 when source belongs to another repository`() = runTest {
    val repo = access.givenRepository()
    val foreign = givenSource(RepositoryId())

    assertNotFound(mockMvc.getAs(access.owner, harvestsUrl(repo, foreign)), "source ${foreign.id.uuid} not found")
    verify(harvestUseCase, never()).findAllBySourceId(any(), any(), any())
  }

  private fun givenSource(repositoryId: RepositoryId): Source {
    val id = SourceId()
    val source = Source(
      id = id,
      title = "Test source",
      repositoryId = repositoryId,
      actions = listOf(FetchAction(sourceId = id, url = "https://example.com")),
    )
    whenever(sourceRepository.findByIdWithActions(eq(id))).thenReturn(source)
    return source
  }

  private fun harvestsUrl(repo: Repository, source: Source) =
    "/api/v1/repositories/${repo.id.uuid}/sources/${source.id.uuid}/harvests"

  private fun harvest(
    sourceId: SourceId = SourceId(),
    logs: String = "log line",
  ) = Harvest(
    id = HarvestId(),
    errornous = false,
    itemsAdded = 3,
    itemsIgnored = 1,
    logs = logs,
    startedAt = LocalDateTime.of(2024, 1, 1, 12, 0),
    finishedAt = LocalDateTime.of(2024, 1, 1, 12, 5),
    sourceId = sourceId,
  )
}
