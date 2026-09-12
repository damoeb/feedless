package org.migor.feedless.http

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.EntityVisibility
import org.migor.feedless.actions.FetchAction
import org.migor.feedless.group.GroupUseCase
import org.migor.feedless.harvest.Harvest
import org.migor.feedless.harvest.HarvestId
import org.migor.feedless.harvest.HarvestStatus
import org.migor.feedless.repository.HarvestService
import org.migor.feedless.http.mapper.HttpHarvestMapper
import org.migor.feedless.http.mapper.HttpScrapeFlowMapper
import org.migor.feedless.repository.Repository
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.repository.RepositoryUseCase
import org.migor.feedless.source.Source
import org.migor.feedless.source.SourceId
import org.migor.feedless.source.SourceRepository
import org.migor.feedless.throttle.Throttled
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
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
@Import(
  HttpHarvestMapper::class,
  HttpScrapeFlowMapper::class,
  HttpApiExceptionHandler::class,
  RepositoryAccessGuard::class,
  RequestContextBridge::class,
)
@ActiveProfiles("test", AppLayer.api, AppProfiles.source, AppProfiles.repository, AppProfiles.user)
class HarvestHttpControllerTest {

  @Autowired
  private lateinit var mockMvc: MockMvc

  @MockitoBean
  private lateinit var harvestUseCase: HarvestService

  @MockitoBean
  private lateinit var sourceRepository: SourceRepository

  @MockitoBean
  private lateinit var repositoryUseCase: RepositoryUseCase

  @MockitoBean
  private lateinit var groupUseCase: GroupUseCase

  private val access by lazy { RepositoryAccessFixture(repositoryUseCase, groupUseCase) }

  @Test
  fun `listHarvests hides dry runs by default`() = runTest {
    val repo = access.givenRepository()
    val source = givenSource(repo.id)
    val harvest = harvest(sourceId = source.id)
    whenever(harvestUseCase.findAllBySourceId(eq(source.id), eq(false), eq(0), eq(20))).thenReturn(listOf(harvest))

    val result = mockMvc.getAs(access.owner, "${harvestsUrl(repo, source)}?page=0&pageSize=20")

    assertStatus(result, 200)
    val body = result.response.contentAsString
    assert(body.contains("\"status\":\"completed\"")) { body }
    assert(body.contains("\"dryRun\":false")) { body }
    assert(body.contains("\"ok\":true")) { body }
    assert(body.contains("\"itemsAdded\":3")) { body }
    assert(body.contains("\"hasMore\":false")) { body }
  }

  @Test
  fun `listHarvests dryRun=true shows only dry runs`() = runTest {
    val repo = access.givenRepository()
    val source = givenSource(repo.id)
    val harvest = harvest(sourceId = source.id, dryRun = true)
    whenever(harvestUseCase.findAllBySourceId(eq(source.id), eq(true), eq(0), eq(20))).thenReturn(listOf(harvest))

    val result = mockMvc.getAs(access.owner, "${harvestsUrl(repo, source)}?page=0&pageSize=20&dryRun=true")

    assertStatus(result, 200)
    assert(result.response.contentAsString.contains("\"dryRun\":true")) { result.response.contentAsString }
  }

  @Test
  fun `listHarvests sets hasMore when a further item exists`() = runTest {
    val repo = access.givenRepository()
    val source = givenSource(repo.id)
    // pageSize + 1 available: there really is a next page
    val harvests = List(3) { harvest(sourceId = source.id) }
    whenever(harvestUseCase.findAllBySourceId(eq(source.id), eq(false), eq(0), eq(2))).thenReturn(harvests)

    val result = mockMvc.getAs(access.owner, "${harvestsUrl(repo, source)}?page=0&pageSize=2")

    assertStatus(result, 200)
    assert(result.response.contentAsString.contains("\"hasMore\":true")) { result.response.contentAsString }
  }

  @Test
  fun `listHarvests reports hasMore false when the last page is exactly full`() = runTest {
    val repo = access.givenRepository()
    val source = givenSource(repo.id)
    // Exactly pageSize available: there is no next page.
    val harvests = List(2) { harvest(sourceId = source.id) }
    whenever(harvestUseCase.findAllBySourceId(eq(source.id), eq(false), eq(0), eq(2))).thenReturn(harvests)

    val result = mockMvc.getAs(access.owner, "${harvestsUrl(repo, source)}?page=0&pageSize=2")

    assertStatus(result, 200)
    assert(result.response.contentAsString.contains("\"hasMore\":false")) { result.response.contentAsString }
  }

  @Test
  fun `listHarvests answers a group member and a stranger on a public repository`() = runTest {
    val private = access.givenRepository()
    val public = access.givenRepository(EntityVisibility.isPublic)
    whenever(harvestUseCase.findAllBySourceId(any(), any(), any(), any())).thenReturn(emptyList())

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
    verify(harvestUseCase, never()).findAllBySourceId(any(), any(), any(), any())
  }

  @Test
  fun `listHarvests returns 404 when source missing`() = runTest {
    val repo = access.givenRepository()
    val sourceId = SourceId()
    whenever(sourceRepository.findByIdWithActions(eq(sourceId))).thenReturn(null)

    val result = mockMvc.getAs(access.owner, "/api/v1/repositories/${repo.id.uuid}/sources/${sourceId.uuid}/harvests")

    assertNotFound(result, "source ${sourceId.uuid} not found")
    verify(harvestUseCase, never()).findAllBySourceId(any(), any(), any(), any())
  }

  @Test
  fun `listHarvests returns 404 when source belongs to another repository`() = runTest {
    val repo = access.givenRepository()
    val foreign = givenSource(RepositoryId())

    assertNotFound(mockMvc.getAs(access.owner, harvestsUrl(repo, foreign)), "source ${foreign.id.uuid} not found")
    verify(harvestUseCase, never()).findAllBySourceId(any(), any(), any(), any())
  }

  @Test
  fun `getHarvest returns status and dryRun`() = runTest {
    val repo = access.givenRepository()
    val source = givenSource(repo.id)
    val harvest = harvest(sourceId = source.id, status = HarvestStatus.RUNNING)
    whenever(harvestUseCase.findById(eq(harvest.id))).thenReturn(harvest)

    val result = mockMvc.getAs(access.owner, harvestUrl(repo, source, harvest))

    assertStatus(result, 200)
    val body = result.response.contentAsString
    assert(body.contains("\"status\":\"running\"")) { body }
    assert(body.contains("\"dryRun\":false")) { body }
  }

  @Test
  fun `getHarvest omits outcome fields while not completed`() = runTest {
    val repo = access.givenRepository()
    val source = givenSource(repo.id)
    val harvest = harvest(sourceId = source.id, status = HarvestStatus.QUEUED)
    whenever(harvestUseCase.findById(eq(harvest.id))).thenReturn(harvest)

    val result = mockMvc.getAs(access.owner, harvestUrl(repo, source, harvest))

    assertStatus(result, 200)
    // Omitted until finished, not reported as false/0.
    val body = result.response.contentAsString
    assert(body.contains("\"ok\":null")) { body }
    assert(body.contains("\"itemsAdded\":null")) { body }
    assert(body.contains("\"itemsIgnored\":null")) { body }
    assert(body.contains("\"finishedAt\":null")) { body }
  }

  @Test
  fun `getHarvest includes outcome fields once completed`() = runTest {
    val repo = access.givenRepository()
    val source = givenSource(repo.id)
    val harvest = harvest(sourceId = source.id, status = HarvestStatus.COMPLETED)
    whenever(harvestUseCase.findById(eq(harvest.id))).thenReturn(harvest)

    val result = mockMvc.getAs(access.owner, harvestUrl(repo, source, harvest))

    assertStatus(result, 200)
    val body = result.response.contentAsString
    assert(body.contains("\"ok\":true")) { body }
    assert(body.contains("\"itemsAdded\":3")) { body }
    assert(body.contains("\"itemsIgnored\":1")) { body }
    assert(body.contains("\"finishedAt\"")) { body }
  }

  @Test
  fun `getHarvest returns 404 when harvest missing`() = runTest {
    val repo = access.givenRepository()
    val source = givenSource(repo.id)
    val harvestId = HarvestId()
    whenever(harvestUseCase.findById(eq(harvestId))).thenReturn(null)

    val result = mockMvc.getAs(access.owner, "${harvestsUrl(repo, source)}/${harvestId.uuid}")

    assertNotFound(result, "harvest ${harvestId.uuid} not found")
  }

  @Test
  fun `getHarvest returns 404 when harvest belongs to another source`() = runTest {
    val repo = access.givenRepository()
    val source = givenSource(repo.id)
    val foreign = harvest(sourceId = SourceId())
    whenever(harvestUseCase.findById(eq(foreign.id))).thenReturn(foreign)

    val result = mockMvc.getAs(access.owner, "${harvestsUrl(repo, source)}/${foreign.id.uuid}")

    assertNotFound(result, "harvest ${foreign.id.uuid} not found")
  }

  @Test
  fun `getHarvest returns 404 when source missing`() = runTest {
    val repo = access.givenRepository()
    val sourceId = SourceId()
    val harvestId = HarvestId()
    whenever(sourceRepository.findByIdWithActions(eq(sourceId))).thenReturn(null)

    val result = mockMvc.getAs(
      access.owner,
      "/api/v1/repositories/${repo.id.uuid}/sources/${sourceId.uuid}/harvests/${harvestId.uuid}",
    )

    assertNotFound(result, "source ${sourceId.uuid} not found")
    verify(harvestUseCase, never()).findById(any())
  }

  @Test
  fun `getHarvest returns 404 when repository access is denied`() = runTest {
    val private = access.givenRepository()
    val source = givenSource(private.id)
    val harvestId = HarvestId()

    val result = mockMvc.getAs(access.stranger, "${harvestsUrl(private, source)}/${harvestId.uuid}")

    assertNotFound(result, "repository ${private.id.uuid} not found")
    verify(harvestUseCase, never()).findById(any())
  }

  @Test
  fun `getHarvestLogs returns the log as text`() = runTest {
    val repo = access.givenRepository()
    val source = givenSource(repo.id)
    val harvest = harvest(sourceId = source.id, logs = "line one\nline two")
    whenever(harvestUseCase.findById(eq(harvest.id))).thenReturn(harvest)

    val result = mockMvc.getAs(access.owner, "${harvestsUrl(repo, source)}/${harvest.id.uuid}/logs")

    assertStatus(result, 200)
    val contentType = result.response.contentType
    assert(contentType?.startsWith("text/plain") == true) { "$contentType" }
    assert(result.response.contentAsString == "line one\nline two") { result.response.contentAsString }
  }

  @Test
  fun `getHarvestLogs returns 404 when harvest missing`() = runTest {
    val repo = access.givenRepository()
    val source = givenSource(repo.id)
    val harvestId = HarvestId()
    whenever(harvestUseCase.findById(eq(harvestId))).thenReturn(null)

    val result = mockMvc.getAs(access.owner, "${harvestsUrl(repo, source)}/${harvestId.uuid}/logs")

    assertNotFound(result, "harvest ${harvestId.uuid} not found")
  }

  @Test
  fun `getHarvestLogs returns 404 when harvest belongs to another source`() = runTest {
    val repo = access.givenRepository()
    val source = givenSource(repo.id)
    val foreign = harvest(sourceId = SourceId())
    whenever(harvestUseCase.findById(eq(foreign.id))).thenReturn(foreign)

    val result = mockMvc.getAs(access.owner, "${harvestsUrl(repo, source)}/${foreign.id.uuid}/logs")

    assertNotFound(result, "harvest ${foreign.id.uuid} not found")
  }

  @Test
  fun `getHarvestLogs returns 404 when repository access is denied`() = runTest {
    val private = access.givenRepository()
    val source = givenSource(private.id)
    val harvestId = HarvestId()

    val result = mockMvc.getAs(access.stranger, "${harvestsUrl(private, source)}/${harvestId.uuid}/logs")

    assertNotFound(result, "repository ${private.id.uuid} not found")
  }

  @Test
  fun `getHarvestLogs returns 404 when source missing`() = runTest {
    val repo = access.givenRepository()
    val sourceId = SourceId()
    val harvestId = HarvestId()
    whenever(sourceRepository.findByIdWithActions(eq(sourceId))).thenReturn(null)

    val result = mockMvc.getAs(
      access.owner,
      "/api/v1/repositories/${repo.id.uuid}/sources/${sourceId.uuid}/harvests/${harvestId.uuid}/logs",
    )

    assertNotFound(result, "source ${sourceId.uuid} not found")
    verify(harvestUseCase, never()).findById(any())
  }

  @Test
  fun `runSource queues a real run and answers 202 with its Location`() = runTest {
    val repo = access.givenRepository()
    val source = givenSource(repo.id)
    val queued = harvest(sourceId = source.id, status = HarvestStatus.QUEUED)
    whenever(harvestUseCase.enqueue(eq(source.id), eq(false), isNull())).thenReturn(queued)

    val result = mockMvc.postAs(access.owner, harvestsUrl(repo, source), """{"dryRun":false}""")

    assertStatus(result, 202)
    assert(result.response.getHeader("Location") == harvestUrl(repo, source, queued)) {
      "${result.response.getHeader("Location")}"
    }
    val body = result.response.contentAsString
    assert(body.contains("\"id\":\"${queued.id.uuid}\"")) { body }
    assert(body.contains("\"status\":\"queued\"")) { body }
    assert(body.contains("\"dryRun\":false")) { body }
    assert(body.contains("\"ok\":null")) { body }
  }

  @Test
  fun `runSource without a body queues a real run`() = runTest {
    val repo = access.givenRepository()
    val source = givenSource(repo.id)
    val queued = harvest(sourceId = source.id, status = HarvestStatus.QUEUED)
    whenever(harvestUseCase.enqueue(eq(source.id), eq(false), isNull())).thenReturn(queued)

    val result = mockMvc.postAs(access.owner, harvestsUrl(repo, source), "")

    assertStatus(result, 202)
  }

  @Test
  fun `runSource queues a dry run with the override flow`() = runTest {
    val repo = access.givenRepository()
    val source = givenSource(repo.id)
    val queued = harvest(sourceId = source.id, status = HarvestStatus.QUEUED, dryRun = true)
    whenever(harvestUseCase.enqueue(eq(source.id), eq(true), anyOrNull())).thenReturn(queued)

    val result = mockMvc.postAs(
      access.owner,
      harvestsUrl(repo, source),
      """{"dryRun":true,"flow":$fixedFlow}""",
    )

    assertStatus(result, 202)
    assert(result.response.contentAsString.contains("\"dryRun\":true")) { result.response.contentAsString }
    val storedFlow = argumentCaptor<String>()
    verify(harvestUseCase).enqueue(eq(source.id), eq(true), storedFlow.capture())
    // The executor reads the flow back through the mapper — it must yield the requested actions.
    val actions = HttpScrapeFlowMapper().storedFlowToDomainActions(storedFlow.firstValue)
    assert((actions.single() as FetchAction).url == "https://example.org/fixed") { "$actions" }
  }

  @Test
  fun `runSource queues a dry run without a flow using the saved one`() = runTest {
    val repo = access.givenRepository()
    val source = givenSource(repo.id)
    val queued = harvest(sourceId = source.id, status = HarvestStatus.QUEUED, dryRun = true)
    whenever(harvestUseCase.enqueue(eq(source.id), eq(true), isNull())).thenReturn(queued)

    assertStatus(mockMvc.postAs(access.owner, harvestsUrl(repo, source), """{"dryRun":true}"""), 202)
  }

  @Test
  fun `runSource rejects a flow without dryRun`() = runTest {
    val repo = access.givenRepository()
    val source = givenSource(repo.id)

    val result = mockMvc.postAs(access.owner, harvestsUrl(repo, source), """{"flow":$fixedFlow}""")

    assertValidationError(result, "only allowed with dryRun: true")
    verify(harvestUseCase, never()).enqueue(any(), any(), anyOrNull())
  }

  @Test
  fun `runSource rejects an invalid flow`() = runTest {
    val repo = access.givenRepository()
    val source = givenSource(repo.id)
    val ambiguous =
      """{"sequence":[{"fetch":{"get":{"url":{"literal":"https://example.org"}}},"purge":{"value":"//div"}}]}"""

    val result = mockMvc.postAs(access.owner, harvestsUrl(repo, source), """{"dryRun":true,"flow":$ambiguous}""")

    assertValidationError(result, "exactly one is allowed")
    verify(harvestUseCase, never()).enqueue(any(), any(), anyOrNull())
  }

  @Test
  fun `runSource answers 409 for a real run of a disabled source`() = runTest {
    val repo = access.givenRepository()
    val source = givenSource(repo.id, disabled = true)

    val result = mockMvc.postAs(access.owner, harvestsUrl(repo, source), "{}")

    assertStatus(result, 409)
    val body = result.response.contentAsString
    assert(body.contains("\"code\":\"CONFLICT\"")) { body }
    assert(body.contains("\"message\":\"source ${source.id.uuid} is disabled")) { body }
    verify(harvestUseCase, never()).enqueue(any(), any(), anyOrNull())
  }

  @Test
  fun `runSource allows a dry run of a disabled source`() = runTest {
    val repo = access.givenRepository()
    val source = givenSource(repo.id, disabled = true)
    val queued = harvest(sourceId = source.id, status = HarvestStatus.QUEUED, dryRun = true)
    whenever(harvestUseCase.enqueue(eq(source.id), eq(true), anyOrNull())).thenReturn(queued)

    val result = mockMvc.postAs(access.owner, harvestsUrl(repo, source), """{"dryRun":true,"flow":$fixedFlow}""")

    assertStatus(result, 202)
  }

  @Test
  fun `runSource accepts a group editor`() = runTest {
    val repo = access.givenRepository()
    val source = givenSource(repo.id)
    val queued = harvest(sourceId = source.id, status = HarvestStatus.QUEUED)
    whenever(harvestUseCase.enqueue(eq(source.id), eq(false), isNull())).thenReturn(queued)

    assertStatus(mockMvc.postAs(access.member, harvestsUrl(repo, source), "{}"), 202)
  }

  @Test
  fun `runSource answers 404 to a stranger, even on a public repository`() = runTest {
    val private = access.givenRepository()
    val public = access.givenRepository(EntityVisibility.isPublic)

    // A dry run is a write too: reading a public repository does not allow running its sources.
    assertNotFound(
      mockMvc.postAs(access.stranger, harvestsUrl(private, givenSource(private.id)), """{"dryRun":true}"""),
      "repository ${private.id.uuid} not found",
    )
    assertNotFound(
      mockMvc.postAs(access.stranger, harvestsUrl(public, givenSource(public.id)), """{"dryRun":true}"""),
      "repository ${public.id.uuid} not found",
    )
    verify(harvestUseCase, never()).enqueue(any(), any(), anyOrNull())
  }

  @Test
  fun `runSource answers 404 for a source of another repository`() = runTest {
    val repo = access.givenRepository()
    val foreign = givenSource(RepositoryId())

    assertNotFound(mockMvc.postAs(access.owner, harvestsUrl(repo, foreign), "{}"), "source ${foreign.id.uuid} not found")
    verify(harvestUseCase, never()).enqueue(any(), any(), anyOrNull())
  }

  @Test
  fun `runSource is throttled like the other write endpoints`() {
    val method = HarvestHttpController::class.java.declaredMethods.first { it.name == "runSource" }
    assert(method.getAnnotation(Throttled::class.java) != null)
  }

  private fun assertValidationError(result: org.springframework.test.web.servlet.MvcResult, message: String) {
    assertStatus(result, 400)
    val body = result.response.contentAsString
    assert(body.contains("\"code\":\"VALIDATION_ERROR\"")) { body }
    assert(body.contains("\"field\":\"flow\"")) { body }
    assert(body.contains(message)) { body }
  }

  private val fixedFlow = """{"sequence":[{"fetch":{"get":{"url":{"literal":"https://example.org/fixed"}}}}]}"""

  private fun givenSource(repositoryId: RepositoryId, disabled: Boolean = false): Source {
    val id = SourceId()
    val source = Source(
      id = id,
      title = "Test source",
      repositoryId = repositoryId,
      disabled = disabled,
      actions = listOf(FetchAction(sourceId = id, url = "https://example.com")),
    )
    whenever(sourceRepository.findByIdWithActions(eq(id))).thenReturn(source)
    return source
  }

  private fun harvestsUrl(repo: Repository, source: Source) =
    "/api/v1/repositories/${repo.id.uuid}/sources/${source.id.uuid}/harvests"

  private fun harvestUrl(repo: Repository, source: Source, harvest: Harvest) =
    "${harvestsUrl(repo, source)}/${harvest.id.uuid}"

  private fun harvest(
    sourceId: SourceId = SourceId(),
    logs: String = "log line",
    status: HarvestStatus = HarvestStatus.COMPLETED,
    dryRun: Boolean = false,
  ) = Harvest(
    id = HarvestId(),
    errornous = false,
    itemsAdded = 3,
    itemsIgnored = 1,
    logs = logs,
    startedAt = LocalDateTime.of(2024, 1, 1, 12, 0),
    finishedAt = LocalDateTime.of(2024, 1, 1, 12, 5),
    sourceId = sourceId,
    status = status,
    dryRun = dryRun,
  )
}
