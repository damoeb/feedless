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
  ETagCalculator::class,
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
  fun `getRepository sets a strong ETag that is stable for the same repository and changes when it does`() = runTest {
    val private = access.givenRepository()

    val first = mockMvc.getAs(access.owner, url(private))
    assertStatus(first, 200)
    val etag = first.response.getHeader("ETag")
    assert(etag != null && etag.startsWith("\"") && etag.endsWith("\"")) { "etag: $etag" }

    val second = mockMvc.getAs(access.owner, url(private))
    assertStatus(second, 200)
    assert(second.response.getHeader("ETag") == etag) { "expected $etag, got ${second.response.getHeader("ETag")}" }

    whenever(repositoryUseCase.findById(eq(private.id))).thenReturn(private.copy(title = "a different title"))
    val third = mockMvc.getAs(access.owner, url(private))
    assertStatus(third, 200)
    assert(third.response.getHeader("ETag") != etag) { "expected a different ETag, got $etag again" }
  }

  @Test
  fun `getRepository keeps the same ETag when only server-owned fields change`() = runTest {
    // A scheduled harvest tick rewrites lastUpdatedAt and nextUpdateAt — none of that is
    // something a caller edited, so it must not invalidate an ETag a
    // `feedctl repo update --editor` session is holding onto.
    val private = access.givenRepository()

    val first = mockMvc.getAs(access.owner, url(private))
    assertStatus(first, 200)
    val etag = first.response.getHeader("ETag")

    whenever(repositoryUseCase.findById(eq(private.id))).thenReturn(
      private.copy(
        lastUpdatedAt = java.time.LocalDateTime.now(),
        triggerScheduledNextAt = java.time.LocalDateTime.now().plusHours(1),
      ),
    )
    val second = mockMvc.getAs(access.owner, url(private))
    assertStatus(second, 200)
    assert(second.response.getHeader("ETag") == etag) { "expected $etag, got ${second.response.getHeader("ETag")}" }
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
  fun `updateRepository without If-Match succeeds unconditionally and returns a new ETag`() = runTest {
    val private = access.givenRepository()

    val result = mockMvc.patchAs(access.owner, url(private), UPDATE)

    assertStatus(result, 200)
    assert(result.response.getHeader("ETag") != null) { "expected an ETag header" }
    verify(repositoryUseCase).updateRepository(any(), any())
  }

  @Test
  fun `updateRepository with a matching If-Match applies the update and returns the new ETag`() = runTest {
    val private = access.givenRepository()
    val etag = requireNotNull(mockMvc.getAs(access.owner, url(private)).response.getHeader("ETag"))

    val result = mockMvc.patchAs(access.owner, url(private), UPDATE, mapOf("If-Match" to etag))

    assertStatus(result, 200)
    assert(result.response.getHeader("ETag") != null) { "expected an ETag header" }
    verify(repositoryUseCase).updateRepository(any(), any())
  }

  @Test
  fun `updateRepository with If-Match star matches any existing repository`() = runTest {
    val private = access.givenRepository()

    val result = mockMvc.patchAs(access.owner, url(private), UPDATE, mapOf("If-Match" to "*"))

    assertStatus(result, 200)
    verify(repositoryUseCase).updateRepository(any(), any())
  }

  @Test
  fun `updateRepository with a stale If-Match answers 412 and never applies the update`() = runTest {
    val private = access.givenRepository()

    val result = mockMvc.patchAs(access.owner, url(private), UPDATE, mapOf("If-Match" to "\"stale\""))

    assertStatus(result, 412)
    val body = result.response.contentAsString
    assert(body.contains("\"code\":\"PRECONDITION_FAILED\"")) { body }
    verify(repositoryUseCase, never()).updateRepository(any(), any())
  }

  @Test
  fun `updateRepository answers a stranger with 404 even with a stale If-Match`() = runTest {
    val private = access.givenRepository()

    val result = mockMvc.patchAs(access.stranger, url(private), UPDATE, mapOf("If-Match" to "\"stale\""))

    assertNotFound(result, "repository ${private.id.uuid} not found")
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
