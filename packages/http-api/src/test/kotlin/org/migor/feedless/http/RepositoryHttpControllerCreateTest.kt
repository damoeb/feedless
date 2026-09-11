package org.migor.feedless.http

import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.migor.feedless.EntityVisibility
import org.migor.feedless.Vertical
import org.migor.feedless.capability.HTTP_API_REQUEST_CONTEXT_ATTR
import org.migor.feedless.capability.RequestContext
import org.migor.feedless.group.GroupId
import org.migor.feedless.http.api.model.RepositoryCreate
import org.migor.feedless.http.api.model.ScrapeAction
import org.migor.feedless.http.api.model.ScrapeFlow
import org.migor.feedless.http.api.model.SourceCreate
import org.migor.feedless.http.mapper.HttpRepositoryMapper
import org.migor.feedless.http.mapper.HttpScrapeFlowMapper
import org.migor.feedless.repository.Repository
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.repository.RepositoryUseCasePort
import org.migor.feedless.user.UserId
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import org.migor.feedless.http.api.model.Vertical as HttpVertical

class RepositoryHttpControllerCreateTest {

  private val repositoryUseCase: RepositoryUseCasePort = mock()
  private val mapper = HttpRepositoryMapper(HttpScrapeFlowMapper())
  // createRepository is not repository-scoped, so it never consults the guard.
  private val controller =
    RepositoryHttpController(repositoryUseCase, mock<RepositoryAccessGuard>(), mapper, ETagCalculator())

  @AfterEach
  fun tearDown() {
    RequestContextHolder.resetRequestAttributes()
  }

  @Test
  fun `createRepository returns the created repository`() = runTest {
    val userId = UserId()
    val repo = repository(userId)
    whenever(repositoryUseCase.create(any())).thenReturn(listOf(repo))

    val response = asUser(userId) { controller.createRepository(repositoryCreate()) }

    assert(response.statusCode == HttpStatus.CREATED)
    assert(response.body!!.title == repo.title)
    verify(repositoryUseCase).create(any())
  }

  @Test
  fun `createRepository returns the shareKey to the owner`() = runTest {
    val userId = UserId()
    whenever(repositoryUseCase.create(any())).thenReturn(listOf(repository(userId)))

    val body = asUser(userId) { controller.createRepository(repositoryCreate()) }.body!!

    assert(body.shareKey == "share-key")
    assert(body.currentUserIsOwner == true)
  }

  @Test
  fun `shareKey is omitted for a non-owner rather than blanked`() = runTest {
    // owned by somebody else
    whenever(repositoryUseCase.create(any())).thenReturn(listOf(repository(UserId())))

    val body = asUser(UserId()) { controller.createRepository(repositoryCreate()) }.body!!

    assert(body.shareKey == null)
    assert(body.currentUserIsOwner == false)
  }

  @Test
  fun `computed counters are omitted rather than reported as zero`() = runTest {
    val userId = UserId()
    whenever(repositoryUseCase.create(any())).thenReturn(listOf(repository(userId)))

    val body = asUser(userId) { controller.createRepository(repositoryCreate()) }.body!!

    assert(body.sourcesCount == null)
    assert(body.sourcesCountWithProblems == null)
  }

  @Test
  fun `visibility is mapped to the public private wire values`() = runTest {
    val userId = UserId()
    whenever(repositoryUseCase.create(any())).thenReturn(listOf(repository(userId)))

    val body = asUser(userId) { controller.createRepository(repositoryCreate()) }.body!!

    assert(body.visibility == org.migor.feedless.http.api.model.Visibility.private)
  }

  /**
   * In production HttpApiServletInvocableHandlerMethod puts the RequestContext into the
   * coroutine context; here we do it directly.
   */
  private suspend fun <T> asUser(userId: UserId, block: suspend () -> T): T {
    val requestContext = RequestContext(groupId = GroupId(), userId = userId)
    bindRequestContext(requestContext)
    return withContext(requestContext) { block() }
  }

  private fun repository(ownerId: UserId) = Repository(
    id = RepositoryId(),
    title = "Created feed",
    description = "desc",
    visibility = EntityVisibility.isPrivate,
    ownerId = ownerId,
    groupId = GroupId(),
    product = Vertical.rssProxy,
    shareKey = "share-key",
  )

  private fun bindRequestContext(requestContext: RequestContext) {
    val request = MockHttpServletRequest()
    request.setAttribute(HTTP_API_REQUEST_CONTEXT_ATTR, requestContext)
    RequestContextHolder.setRequestAttributes(ServletRequestAttributes(request))
  }

  private fun repositoryCreate() = RepositoryCreate(
    product = HttpVertical.rssProxy,
    title = "Created feed",
    description = "desc",
    sources = listOf(sourceCreate()),
  )

  private fun sourceCreate() = SourceCreate(
    title = "Source 1",
    flow = ScrapeFlow(
      sequence = listOf(
        ScrapeAction(
          fetch = org.migor.feedless.http.api.model.HttpFetch(
            get = org.migor.feedless.http.api.model.HttpGetRequest(
              url = org.migor.feedless.http.api.model.StringLiteralOrVariable(literal = "https://example.com"),
            ),
          ),
        ),
      ),
    ),
  )
}
