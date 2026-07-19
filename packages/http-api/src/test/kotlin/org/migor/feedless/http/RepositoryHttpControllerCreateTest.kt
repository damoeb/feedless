package org.migor.feedless.http

import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
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
  private val controller = RepositoryHttpController(repositoryUseCase, mapper)

  @AfterEach
  fun tearDown() {
    RequestContextHolder.resetRequestAttributes()
  }

  @Test
  fun `createRepositories runs flow with request context from servlet attribute`() = runTest {
    val userId = UserId()
    val requestContext = RequestContext(groupId = GroupId(), userId = userId)
    bindRequestContext(requestContext)

    val repo = Repository(
      id = RepositoryId(),
      title = "Created feed",
      description = "desc",
      visibility = EntityVisibility.isPrivate,
      ownerId = userId,
      groupId = GroupId(),
      product = Vertical.rssProxy,
      shareKey = "share-key",
    )
    whenever(repositoryUseCase.create(any())).thenReturn(listOf(repo))

    val response = controller.createRepositories(flowOf(repositoryCreate()))
    val body = response.body!!.toList()

    assert(response.statusCode == HttpStatus.CREATED)
    assert(body.size == 1)
    assert(body.first().title == repo.title)
    verify(repositoryUseCase).create(any())
  }

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
