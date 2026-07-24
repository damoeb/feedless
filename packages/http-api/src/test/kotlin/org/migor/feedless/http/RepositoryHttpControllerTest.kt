package org.migor.feedless.http

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.EntityVisibility
import org.migor.feedless.HostOverloadingException
import org.migor.feedless.Vertical
import org.migor.feedless.group.GroupId
import org.migor.feedless.http.mapper.HttpRepositoryMapper
import org.migor.feedless.http.mapper.HttpScrapeFlowMapper
import org.migor.feedless.repository.Repository
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.repository.RepositoryUseCasePort
import org.migor.feedless.user.UserId
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
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
import java.time.Duration

@WebMvcTest(controllers = [RepositoryHttpController::class])
@AutoConfigureMockMvc(addFilters = false)
@Import(HttpRepositoryMapper::class, HttpScrapeFlowMapper::class, HttpApiExceptionHandler::class)
@ActiveProfiles("test", AppLayer.api, AppProfiles.repository)
class RepositoryHttpControllerTest {

  @Autowired
  private lateinit var mockMvc: MockMvc

  @MockitoBean
  private lateinit var repositoryUseCase: RepositoryUseCasePort

  @Test
  fun `listRepositories returns items`() = runTest {
    val userId = UserId()
    val repo = Repository(
      id = RepositoryId(),
      title = "Test feed",
      description = "desc",
      visibility = EntityVisibility.isPrivate,
      ownerId = userId,
      groupId = GroupId(),
      product = Vertical.rssProxy,
      shareKey = "share-key",
    )
    whenever(repositoryUseCase.findAllByUserId(any(), anyOrNull(), anyOrNull())).thenReturn(listOf(repo))

    val mvcResult = mockMvc.get("/api/v1/repositories") {
      param("page", "0")
      param("pageSize", "20")
    }.andReturn()

    if (mvcResult.request.asyncContext != null) {
      mockMvc.perform(asyncDispatch(mvcResult))
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.items[0].id").value(repo.id.uuid.toString()))
        .andExpect(jsonPath("$.items[0].title").value(repo.title))
    } else {
      assert(mvcResult.response.status == 200)
      assert(mvcResult.response.contentAsString.contains(repo.id.uuid.toString()))
      assert(mvcResult.response.contentAsString.contains(repo.title))
    }
  }

  @Test
  fun `a missing repository answers 404 with an ApiError body`() = runTest {
    val repositoryId = RepositoryId()
    whenever(repositoryUseCase.findById(eq(repositoryId))).thenReturn(null)

    val mvcResult = mockMvc.get("/api/v1/repositories/${repositoryId.uuid}").andReturn()

    val result = if (mvcResult.request.asyncContext != null) {
      mockMvc.perform(asyncDispatch(mvcResult)).andReturn()
    } else {
      mvcResult
    }
    assert(result.response.status == 404)
    // Used to be an empty body from ResponseEntity.notFound(), so clients parsing .code got null.
    val body = result.response.contentAsString
    assert(body.contains("\"code\":\"NOT_FOUND\"")) { body }
    assert(body.contains("\"corrId\"")) { body }
  }

  @Test
  fun `an exhausted rate limit answers 429 with Retry-After`() = runTest {
    whenever(repositoryUseCase.findAllByUserId(any(), anyOrNull(), anyOrNull()))
      .thenThrow(HostOverloadingException("You have exhausted your API Request Quota", Duration.ofSeconds(42)))

    val mvcResult = mockMvc.get("/api/v1/repositories").andReturn()

    val result = if (mvcResult.request.asyncContext != null) {
      mockMvc.perform(asyncDispatch(mvcResult)).andReturn()
    } else {
      mvcResult
    }
    // Previously fell through to handleGeneric and looked like a 500.
    assert(result.response.status == 429) { "was ${result.response.status}" }
    assert(result.response.getHeader("Retry-After") == "42")
    assert(result.response.contentAsString.contains("\"code\":\"TOO_MANY_REQUESTS\""))
  }
}
