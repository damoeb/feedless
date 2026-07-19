package org.migor.feedless.http

import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.migor.feedless.actions.FetchAction
import org.migor.feedless.capability.HTTP_API_REQUEST_CONTEXT_ATTR
import org.migor.feedless.capability.RequestContext
import org.migor.feedless.group.GroupId
import org.migor.feedless.http.api.model.ScrapeAction
import org.migor.feedless.http.api.model.ScrapeFlow
import org.migor.feedless.http.api.model.SourceCreate
import org.migor.feedless.http.mapper.HttpScrapeFlowMapper
import org.migor.feedless.http.mapper.HttpSourceMapper
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.source.Source
import org.migor.feedless.source.SourceId
import org.migor.feedless.source.SourceRepository
import org.migor.feedless.source.SourceUseCasePort
import org.migor.feedless.user.UserId
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.util.UUID

class SourceHttpControllerCreateTest {

  private val sourceUseCase: SourceUseCasePort = mock()
  private val sourceRepository: SourceRepository = mock()
  private val mapper = HttpSourceMapper(HttpScrapeFlowMapper())
  private val controller = SourceHttpController(sourceUseCase, sourceRepository, mapper)

  @AfterEach
  fun tearDown() {
    RequestContextHolder.resetRequestAttributes()
  }

  @Test
  fun `createSources runs flow with request context from servlet attribute`() = runTest {
    val repoId = UUID.randomUUID()
    val requestContext = RequestContext(groupId = GroupId(), userId = UserId())
    bindRequestContext(requestContext)

    val sourceId = SourceId()
    val created = Source(
      id = sourceId,
      title = "Test source",
      repositoryId = RepositoryId(repoId.toString()),
      actions = listOf(FetchAction(sourceId = sourceId, url = "https://example.com")),
    )
    whenever(sourceUseCase.createSources(any(), any())).thenReturn(listOf(created))

    val response = controller.createSources(repoId, flowOf(sourceCreate()))
    val body = response.body!!.toList()

    assert(response.statusCode == HttpStatus.CREATED)
    assert(body.size == 1)
    assert(body.first().title == created.title)
    verify(sourceUseCase).createSources(any(), any())
  }

  private fun bindRequestContext(requestContext: RequestContext) {
    val request = MockHttpServletRequest()
    request.setAttribute(HTTP_API_REQUEST_CONTEXT_ATTR, requestContext)
    RequestContextHolder.setRequestAttributes(ServletRequestAttributes(request))
  }

  private fun sourceCreate() = SourceCreate(
    title = "Test source",
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
