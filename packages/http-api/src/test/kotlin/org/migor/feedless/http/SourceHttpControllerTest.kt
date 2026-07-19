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
    val repoId = java.util.UUID.randomUUID()
    val source = Source(
      id = SourceId(),
      title = "Test source",
      repositoryId = RepositoryId(repoId.toString()),
      actions = listOf(
        FetchAction(sourceId = SourceId(), url = "https://example.com"),
      ),
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
}
