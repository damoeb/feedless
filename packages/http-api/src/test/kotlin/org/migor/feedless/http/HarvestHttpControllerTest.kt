package org.migor.feedless.http

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.actions.FetchAction
import org.migor.feedless.harvest.Harvest
import org.migor.feedless.harvest.HarvestId
import org.migor.feedless.harvest.HarvestUseCasePort
import org.migor.feedless.http.mapper.HttpHarvestMapper
import org.migor.feedless.repository.RepositoryId
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
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime
import java.util.UUID

@WebMvcTest(controllers = [HarvestHttpController::class])
@AutoConfigureMockMvc(addFilters = false)
@Import(HttpHarvestMapper::class, HttpApiExceptionHandler::class)
@ActiveProfiles("test", AppLayer.api, AppProfiles.source)
class HarvestHttpControllerTest {

  @Autowired
  private lateinit var mockMvc: MockMvc

  @MockitoBean
  private lateinit var harvestUseCase: HarvestUseCasePort

  @MockitoBean
  private lateinit var sourceRepository: SourceRepository

  @Test
  fun `listHarvests returns items and omits logs by default`() = runTest {
    val repoId = UUID.randomUUID()
    val sourceId = SourceId()
    whenever(sourceRepository.findByIdWithActions(eq(sourceId))).thenReturn(
      source(id = sourceId, repositoryId = RepositoryId(repoId.toString())),
    )
    val harvest = harvest(sourceId = sourceId, logs = "very long harvest log output")
    whenever(harvestUseCase.findAllBySourceId(eq(sourceId), eq(0), eq(20))).thenReturn(listOf(harvest))

    val mvcResult = mockMvc.get("/api/v1/repositories/$repoId/sources/${sourceId.uuid}/harvests") {
      param("page", "0")
      param("pageSize", "20")
    }.andReturn()

    if (mvcResult.request.asyncContext != null) {
      mockMvc.perform(asyncDispatch(mvcResult))
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.items[0].ok").value(true))
        .andExpect(jsonPath("$.items[0].itemsAdded").value(3))
        .andExpect(jsonPath("$.items[0].logs").value(""))
        .andExpect(jsonPath("$.hasMore").value(false))
    } else {
      assert(mvcResult.response.status == 200)
      assert(mvcResult.response.contentAsString.contains("\"logs\":\"\""))
      assert(!mvcResult.response.contentAsString.contains("very long harvest log output"))
    }
  }

  @Test
  fun `listHarvests includes logs when includeLogs is true`() = runTest {
    val repoId = UUID.randomUUID()
    val sourceId = SourceId()
    whenever(sourceRepository.findByIdWithActions(eq(sourceId))).thenReturn(
      source(id = sourceId, repositoryId = RepositoryId(repoId.toString())),
    )
    val harvest = harvest(sourceId = sourceId, logs = "full harvest log output")
    whenever(harvestUseCase.findAllBySourceId(eq(sourceId), eq(0), eq(20))).thenReturn(listOf(harvest))

    val mvcResult = mockMvc.get("/api/v1/repositories/$repoId/sources/${sourceId.uuid}/harvests") {
      param("page", "0")
      param("pageSize", "20")
      param("includeLogs", "true")
    }.andReturn()

    if (mvcResult.request.asyncContext != null) {
      mockMvc.perform(asyncDispatch(mvcResult))
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.items[0].logs").value("full harvest log output"))
    } else {
      assert(mvcResult.response.status == 200)
      assert(mvcResult.response.contentAsString.contains("full harvest log output"))
    }
  }

  @Test
  fun `listHarvests sets hasMore when page is full`() = runTest {
    val repoId = UUID.randomUUID()
    val sourceId = SourceId()
    whenever(sourceRepository.findByIdWithActions(eq(sourceId))).thenReturn(
      source(id = sourceId, repositoryId = RepositoryId(repoId.toString())),
    )
    val harvests = List(2) { harvest(sourceId = sourceId) }
    whenever(harvestUseCase.findAllBySourceId(eq(sourceId), eq(0), eq(2))).thenReturn(harvests)

    val mvcResult = mockMvc.get("/api/v1/repositories/$repoId/sources/${sourceId.uuid}/harvests") {
      param("page", "0")
      param("pageSize", "2")
    }.andReturn()

    if (mvcResult.request.asyncContext != null) {
      mockMvc.perform(asyncDispatch(mvcResult))
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.hasMore").value(true))
        .andExpect(jsonPath("$.items.length()").value(2))
    } else {
      assert(mvcResult.response.status == 200)
      assert(mvcResult.response.contentAsString.contains("\"hasMore\":true"))
    }
  }

  @Test
  fun `listHarvests returns 404 when source missing`() = runTest {
    val repoId = UUID.randomUUID()
    val sourceId = SourceId()
    whenever(sourceRepository.findByIdWithActions(eq(sourceId))).thenReturn(null)

    val mvcResult = mockMvc.get("/api/v1/repositories/$repoId/sources/${sourceId.uuid}/harvests").andReturn()

    dispatchIfAsync(mvcResult, status().isNotFound)
    verify(harvestUseCase, never()).findAllBySourceId(any(), any(), any())
  }

  @Test
  fun `listHarvests returns 404 when source belongs to another repository`() = runTest {
    val repoId = UUID.randomUUID()
    val otherRepoId = UUID.randomUUID()
    val sourceId = SourceId()
    whenever(sourceRepository.findByIdWithActions(eq(sourceId))).thenReturn(
      source(id = sourceId, repositoryId = RepositoryId(otherRepoId.toString())),
    )

    val mvcResult = mockMvc.get("/api/v1/repositories/$repoId/sources/${sourceId.uuid}/harvests").andReturn()

    dispatchIfAsync(mvcResult, status().isNotFound)
    verify(harvestUseCase, never()).findAllBySourceId(any(), any(), any())
  }

  private fun source(
    id: SourceId = SourceId(),
    repositoryId: RepositoryId = RepositoryId(UUID.randomUUID().toString()),
  ) = Source(
    id = id,
    title = "Test source",
    repositoryId = repositoryId,
    actions = listOf(
      FetchAction(sourceId = id, url = "https://example.com"),
    ),
  )

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

  private fun dispatchIfAsync(
    mvcResult: org.springframework.test.web.servlet.MvcResult,
    expectedStatus: org.springframework.test.web.servlet.ResultMatcher,
  ) {
    if (mvcResult.request.asyncContext != null) {
      mockMvc.perform(asyncDispatch(mvcResult)).andExpect(expectedStatus)
    } else {
      expectedStatus.match(mvcResult)
    }
  }
}
