package org.migor.feedless.http

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.EntityVisibility
import org.migor.feedless.HostOverloadingException
import org.migor.feedless.Vertical
import org.migor.feedless.group.GroupId
import org.migor.feedless.group.GroupUseCasePort
import org.migor.feedless.http.mapper.HttpRepositoryMapper
import org.migor.feedless.http.mapper.HttpScrapeFlowMapper
import org.migor.feedless.repository.Repository
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.repository.RepositoryUseCasePort
import org.migor.feedless.source.SourceRepository
import org.migor.feedless.user.UserId
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
@Import(
  HttpRepositoryMapper::class,
  HttpScrapeFlowMapper::class,
  HttpApiExceptionHandler::class,
  RepositoryAccessGuard::class,
  RequestContextBridge::class,
)
@ActiveProfiles("test", AppLayer.api, AppProfiles.repository, AppProfiles.source, AppProfiles.user)
class RepositoryHttpControllerTest {

  @Autowired
  private lateinit var mockMvc: MockMvc

  @MockitoBean
  private lateinit var repositoryUseCase: RepositoryUseCasePort

  @MockitoBean
  private lateinit var groupUseCase: GroupUseCasePort

  @MockitoBean
  private lateinit var sourceRepository: SourceRepository

  private val access by lazy { RepositoryAccessFixture(repositoryUseCase, groupUseCase) }

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
    whenever(repositoryUseCase.countAllByUserId(anyOrNull(), anyOrNull())).thenReturn(1)

    val mvcResult = mockMvc.get("/api/v1/repositories") {
      param("page", "0")
      param("pageSize", "20")
    }.andReturn()

    if (mvcResult.request.asyncContext != null) {
      mockMvc.perform(asyncDispatch(mvcResult))
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.items[0].id").value(repo.id.uuid.toString()))
        .andExpect(jsonPath("$.items[0].title").value(repo.title))
        .andExpect(jsonPath("$.totalCount").value(1))
    } else {
      assert(mvcResult.response.status == 200)
      assert(mvcResult.response.contentAsString.contains(repo.id.uuid.toString()))
      assert(mvcResult.response.contentAsString.contains(repo.title))
      assert(mvcResult.response.contentAsString.contains("\"totalCount\":1"))
    }
  }

  @Test
  fun `a missing repository answers 404 with an ApiError body`() = runTest {
    val repositoryId = RepositoryId()
    whenever(repositoryUseCase.findById(eq(repositoryId))).thenReturn(null)

    val result = mockMvc.getAs(UserId(), "/api/v1/repositories/${repositoryId.uuid}")

    // Used to be an empty body from ResponseEntity.notFound(), so clients parsing .code got null.
    assertNotFound(result, "repository ${repositoryId.uuid} not found")
    assert(result.response.contentAsString.contains("\"corrId\"")) { result.response.contentAsString }
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

  @Test
  fun `getRepository answers the owner, a group member, and a stranger on a public repository`() = runTest {
    val private = access.givenRepository()
    val public = access.givenRepository(EntityVisibility.isPublic)

    assertStatus(mockMvc.getAs(access.owner, url(private)), 200)
    assertStatus(mockMvc.getAs(access.member, url(private)), 200)
    assertStatus(mockMvc.getAs(access.stranger, url(public)), 200)
  }

  @Test
  fun `getRepository answers a stranger on a private repository like a missing one`() = runTest {
    val private = access.givenRepository()

    assertNotFound(mockMvc.getAs(access.stranger, url(private)), "repository ${private.id.uuid} not found")
  }

  @Test
  // The use case is mocked: this proves the guard lets both through, not that server-core
  // accepts the write (it does not yet for a group member — see the http-api README).
  fun `updateRepository lets the owner and a group member through the guard to the use case`() = runTest {
    val private = access.givenRepository()

    assertStatus(mockMvc.patchAs(access.owner, url(private), UPDATE), 200)
    assertStatus(mockMvc.patchAs(access.member, url(private), UPDATE), 200)
  }

  @Test
  fun `updateRepository answers a stranger with 404 even on a public repository`() = runTest {
    val private = access.givenRepository()
    val public = access.givenRepository(EntityVisibility.isPublic)

    assertNotFound(mockMvc.patchAs(access.stranger, url(private), UPDATE), "repository ${private.id.uuid} not found")
    assertNotFound(mockMvc.patchAs(access.stranger, url(public), UPDATE), "repository ${public.id.uuid} not found")
    verify(repositoryUseCase, never()).updateRepository(any(), any())
  }

  @Test
  // The use case is mocked: this proves the guard lets both through, not that server-core
  // deletes for a group member (it answers 403 today — see the http-api README).
  fun `deleteRepository lets the owner and a group member through the guard to the use case`() = runTest {
    val private = access.givenRepository()

    assertStatus(mockMvc.deleteAs(access.owner, url(private)), 204)
    assertStatus(mockMvc.deleteAs(access.member, url(private)), 204)
  }

  @Test
  fun `deleteRepository answers a stranger with 404 before the use case can answer 403`() = runTest {
    val private = access.givenRepository()
    val public = access.givenRepository(EntityVisibility.isPublic)

    assertNotFound(mockMvc.deleteAs(access.stranger, url(private)), "repository ${private.id.uuid} not found")
    assertNotFound(mockMvc.deleteAs(access.stranger, url(public)), "repository ${public.id.uuid} not found")
    verify(repositoryUseCase, never()).delete(any())
  }

  private fun url(repo: Repository) = "/api/v1/repositories/${repo.id.uuid}"

  private companion object {
    const val UPDATE = """{"title":"updated"}"""
  }
}
