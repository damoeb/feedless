package org.migor.feedless.http

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.actions.FetchAction
import org.migor.feedless.http.mapper.HttpScrapeFlowMapper
import org.migor.feedless.http.mapper.HttpSourceMapper
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.source.Source
import org.migor.feedless.source.SourceId
import org.migor.feedless.source.SourceRepository
import org.migor.feedless.source.SourceUseCasePort
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

@WebMvcTest(controllers = [SourceHttpController::class])
@AutoConfigureMockMvc(addFilters = false)
@Import(HttpSourceMapper::class, HttpScrapeFlowMapper::class, HttpApiExceptionHandler::class)
@ActiveProfiles("test", AppLayer.api, AppProfiles.source)
class SourceHttpControllerTest {

  @Autowired
  private lateinit var mockMvc: MockMvc

  @MockitoBean
  private lateinit var sourceRepository: SourceRepository

  @MockitoBean
  private lateinit var sourceUseCase: SourceUseCasePort

  @Test
  fun `listSources returns items for repository`() = runTest {
    val repoId = UUID.randomUUID()
    val source = source(
      repositoryId = RepositoryId(repoId.toString()),
    )
    whenever(
      sourceRepository.findAllByRepositoryIdFiltered(any(), any(), anyOrNull(), anyOrNull()),
    ).thenReturn(listOf(source))

    val mvcResult = mockMvc.get("/api/v1/repositories/$repoId/sources") {
      param("page", "0")
      param("pageSize", "20")
    }.andReturn()

    if (mvcResult.request.asyncContext != null) {
      mockMvc.perform(asyncDispatch(mvcResult))
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.items[0].id").value(source.id.uuid.toString()))
    } else {
      assert(mvcResult.response.status == 200)
      assert(mvcResult.response.contentAsString.contains(source.id.uuid.toString()))
    }
  }

  @Test
  fun `updateSource returns 404 when source missing`() = runTest {
    val repoId = UUID.randomUUID()
    val sourceId = UUID.randomUUID()
    whenever(sourceRepository.findByIdWithActions(eq(SourceId(sourceId.toString())))).thenReturn(null)

    val mvcResult = mockMvc.patch("/api/v1/repositories/$repoId/sources/$sourceId") {
      contentType = MediaType.APPLICATION_JSON
      content = """{"title":"updated"}"""
    }.andReturn()

    dispatchIfAsync(mvcResult, status().isNotFound)
    verify(sourceUseCase, never()).updateSources(any(), any())
  }

  @Test
  fun `updateSource returns 404 when source belongs to another repository`() = runTest {
    val repoId = UUID.randomUUID()
    val otherRepoId = UUID.randomUUID()
    val sourceId = UUID.randomUUID()
    whenever(sourceRepository.findByIdWithActions(eq(SourceId(sourceId.toString())))).thenReturn(
      source(
        id = SourceId(sourceId.toString()),
        repositoryId = RepositoryId(otherRepoId.toString()),
      ),
    )

    val mvcResult = mockMvc.patch("/api/v1/repositories/$repoId/sources/$sourceId") {
      contentType = MediaType.APPLICATION_JSON
      content = """{"title":"updated"}"""
    }.andReturn()

    dispatchIfAsync(mvcResult, status().isNotFound)
    verify(sourceUseCase, never()).updateSources(any(), any())
  }

  @Test
  fun `deleteSource returns 404 when source missing`() = runTest {
    val repoId = UUID.randomUUID()
    val sourceId = UUID.randomUUID()
    whenever(sourceRepository.findByIdWithActions(eq(SourceId(sourceId.toString())))).thenReturn(null)

    val mvcResult = mockMvc.delete("/api/v1/repositories/$repoId/sources/$sourceId").andReturn()

    dispatchIfAsync(mvcResult, status().isNotFound)
    verify(sourceUseCase, never()).deleteAllById(any(), any())
  }

  @Test
  fun `deleteSource returns 404 when source belongs to another repository`() = runTest {
    val repoId = UUID.randomUUID()
    val otherRepoId = UUID.randomUUID()
    val sourceId = UUID.randomUUID()
    whenever(sourceRepository.findByIdWithActions(eq(SourceId(sourceId.toString())))).thenReturn(
      source(
        id = SourceId(sourceId.toString()),
        repositoryId = RepositoryId(otherRepoId.toString()),
      ),
    )

    val mvcResult = mockMvc.delete("/api/v1/repositories/$repoId/sources/$sourceId").andReturn()

    dispatchIfAsync(mvcResult, status().isNotFound)
    verify(sourceUseCase, never()).deleteAllById(any(), any())
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
