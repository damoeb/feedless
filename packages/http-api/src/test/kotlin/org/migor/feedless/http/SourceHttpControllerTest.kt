package org.migor.feedless.http

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.EntityVisibility
import org.migor.feedless.actions.FetchAction
import org.migor.feedless.group.GroupId
import org.migor.feedless.group.GroupUseCasePort
import org.migor.feedless.http.mapper.HttpScrapeFlowMapper
import org.migor.feedless.http.mapper.HttpSourceMapper
import org.migor.feedless.repository.Repository
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.repository.RepositoryUseCasePort
import org.migor.feedless.source.Source
import org.migor.feedless.source.SourceId
import org.migor.feedless.source.SourceRepository
import org.migor.feedless.source.SourceUseCasePort
import org.migor.feedless.source.SourcesFilter
import org.migor.feedless.userGroup.RoleInGroup
import org.migor.feedless.userGroup.UserGroupAssignment
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

@WebMvcTest(controllers = [SourceHttpController::class])
@AutoConfigureMockMvc(addFilters = false)
@Import(
  HttpSourceMapper::class,
  HttpScrapeFlowMapper::class,
  HttpApiExceptionHandler::class,
  RepositoryAccessGuard::class,
  RequestContextBridge::class,
)
@ActiveProfiles("test", AppLayer.api, AppProfiles.source, AppProfiles.repository, AppProfiles.user)
class SourceHttpControllerTest {

  @Autowired
  private lateinit var mockMvc: MockMvc

  @MockitoBean
  private lateinit var sourceRepository: SourceRepository

  @MockitoBean
  private lateinit var sourceUseCase: SourceUseCasePort

  @MockitoBean
  private lateinit var repositoryUseCase: RepositoryUseCasePort

  @MockitoBean
  private lateinit var groupUseCase: GroupUseCasePort

  private val access by lazy { RepositoryAccessFixture(repositoryUseCase, groupUseCase) }

  @Test
  fun `listSources answers the owner, a group member, and a stranger on a public repository`() = runTest {
    val private = access.givenRepository()
    val public = access.givenRepository(EntityVisibility.isPublic)
    val source = givenSource(private.id)
    whenever(sourceRepository.findAllByRepositoryIdFiltered(any(), any(), anyOrNull(), anyOrNull()))
      .thenReturn(listOf(source))

    val result = mockMvc.getAs(access.owner, sourcesUrl(private))
    assertStatus(result, 200)
    assert(result.response.contentAsString.contains(source.id.uuid.toString())) { result.response.contentAsString }
    assert(result.response.contentAsString.contains("\"repositoryId\":\"${private.id.uuid}\"")) { result.response.contentAsString }
    assert(result.response.contentAsString.contains("\"errorsInSuccession\":${source.errorsInSuccession}")) { result.response.contentAsString }
    assertStatus(mockMvc.getAs(access.member, sourcesUrl(private)), 200)
    assertStatus(mockMvc.getAs(access.stranger, sourcesUrl(public)), 200)
  }

  @Test
  fun `listSources answers a stranger on a private repository like a missing one`() = runTest {
    val private = access.givenRepository()

    assertNotFound(mockMvc.getAs(access.stranger, sourcesUrl(private)), "repository ${private.id.uuid} not found")
    verify(sourceRepository, never()).findAllByRepositoryIdFiltered(any(), any(), anyOrNull(), anyOrNull())
  }

  @Test
  fun `listSources passes minErrorsInSuccession through as a SourcesFilter`() = runTest {
    val private = access.givenRepository()
    whenever(sourceRepository.findAllByRepositoryIdFiltered(any(), any(), anyOrNull(), anyOrNull()))
      .thenReturn(emptyList())

    mockMvc.getAs(access.owner, "${sourcesUrl(private)}?minErrorsInSuccession=3")

    verify(sourceRepository).findAllByRepositoryIdFiltered(
      eq(private.id),
      any(),
      eq(SourcesFilter(minErrorsInSuccession = 3)),
      anyOrNull(),
    )
  }

  @Test
  fun `listUserSources returns sources across every repository the caller owns or belongs to`() = runTest {
    val groupId = GroupId()
    whenever(groupUseCase.findAllByUserId(eq(access.owner))).thenReturn(
      listOf(UserGroupAssignment(role = RoleInGroup.editor, userId = access.owner, groupId = groupId)),
    )
    val source = givenSource(RepositoryId())
    whenever(sourceRepository.findAllForUser(eq(access.owner), eq(listOf(groupId)), any(), anyOrNull()))
      .thenReturn(listOf(source))

    val result = mockMvc.getAs(access.owner, "/api/v1/user/sources")

    assertStatus(result, 200)
    assert(result.response.contentAsString.contains(source.id.uuid.toString())) { result.response.contentAsString }
  }

  @Test
  fun `listUserSources passes disabled, like, and minErrorsInSuccession through as a SourcesFilter`() = runTest {
    whenever(groupUseCase.findAllByUserId(eq(access.owner))).thenReturn(emptyList())
    whenever(sourceRepository.findAllForUser(any(), any(), any(), anyOrNull())).thenReturn(emptyList())

    mockMvc.getAs(access.owner, "/api/v1/user/sources?disabled=true&like=foo&minErrorsInSuccession=1")

    verify(sourceRepository).findAllForUser(
      eq(access.owner),
      eq(emptyList()),
      any(),
      eq(SourcesFilter(disabled = true, like = "foo", minErrorsInSuccession = 1)),
    )
  }

  @Test
  fun `listUserSources answers a caller without a user id like a denied repository`() = runTest {
    assertNotFound(mockMvc.getAnonymous("/api/v1/user/sources"), "user not found")
    verify(sourceRepository, never()).findAllForUser(any(), any(), any(), anyOrNull())
  }

  @Test
  fun `getSource answers a group member and a stranger on a public repository`() = runTest {
    val private = access.givenRepository()
    val public = access.givenRepository(EntityVisibility.isPublic)

    assertStatus(mockMvc.getAs(access.member, sourceUrl(private, givenSource(private.id))), 200)
    assertStatus(mockMvc.getAs(access.stranger, sourceUrl(public, givenSource(public.id))), 200)
  }

  @Test
  fun `getSource answers a stranger on a private repository like a missing one`() = runTest {
    val private = access.givenRepository()
    val source = givenSource(private.id)

    assertNotFound(mockMvc.getAs(access.stranger, sourceUrl(private, source)), "repository ${private.id.uuid} not found")
  }

  @Test
  fun `createSource lets the owner and a group member through the guard to the use case`() = runTest {
    val private = access.givenRepository()
    val created = givenSource(private.id)
    whenever(sourceUseCase.createSources(any(), eq(private.id))).thenReturn(listOf(created))

    assertStatus(mockMvc.postAs(access.owner, sourcesUrl(private), CREATE), 201)
    assertStatus(mockMvc.postAs(access.member, sourcesUrl(private), CREATE), 201)
  }

  @Test
  fun `createSource answers a stranger with 404 even on a public repository`() = runTest {
    val private = access.givenRepository()
    val public = access.givenRepository(EntityVisibility.isPublic)

    assertNotFound(mockMvc.postAs(access.stranger, sourcesUrl(private), CREATE), "repository ${private.id.uuid} not found")
    assertNotFound(mockMvc.postAs(access.stranger, sourcesUrl(public), CREATE), "repository ${public.id.uuid} not found")
    verify(sourceUseCase, never()).createSources(any(), any())
  }

  @Test
  fun `updateSource lets the owner and a group member through the guard to the use case`() = runTest {
    val private = access.givenRepository()
    val source = givenSource(private.id)

    assertStatus(mockMvc.patchAs(access.owner, sourceUrl(private, source), UPDATE), 200)
    assertStatus(mockMvc.patchAs(access.member, sourceUrl(private, source), UPDATE), 200)
  }

  @Test
  fun `updateSource answers a stranger with 404 even on a public repository`() = runTest {
    val private = access.givenRepository()
    val public = access.givenRepository(EntityVisibility.isPublic)

    assertNotFound(
      mockMvc.patchAs(access.stranger, sourceUrl(private, givenSource(private.id)), UPDATE),
      "repository ${private.id.uuid} not found",
    )
    assertNotFound(
      mockMvc.patchAs(access.stranger, sourceUrl(public, givenSource(public.id)), UPDATE),
      "repository ${public.id.uuid} not found",
    )
    verify(sourceUseCase, never()).updateSources(any(), any())
  }

  @Test
  fun `updateSource returns 404 when source missing`() = runTest {
    val private = access.givenRepository()
    val sourceId = SourceId()
    whenever(sourceRepository.findByIdWithActions(eq(sourceId))).thenReturn(null)

    val result = mockMvc.patchAs(access.owner, "${sourcesUrl(private)}/${sourceId.uuid}", UPDATE)

    assertNotFound(result, "source ${sourceId.uuid} not found")
    verify(sourceUseCase, never()).updateSources(any(), any())
  }

  @Test
  fun `updateSource returns 404 when source belongs to another repository`() = runTest {
    val private = access.givenRepository()
    val foreign = givenSource(RepositoryId())

    val result = mockMvc.patchAs(access.owner, sourceUrl(private, foreign), UPDATE)

    assertNotFound(result, "source ${foreign.id.uuid} not found")
    verify(sourceUseCase, never()).updateSources(any(), any())
  }

  @Test
  fun `deleteSource lets the owner and a group member through the guard to the use case`() = runTest {
    val private = access.givenRepository()
    val source = givenSource(private.id)

    assertStatus(mockMvc.deleteAs(access.owner, sourceUrl(private, source)), 204)
    assertStatus(mockMvc.deleteAs(access.member, sourceUrl(private, source)), 204)
  }

  @Test
  fun `deleteSource answers a stranger with 404 even on a public repository`() = runTest {
    val private = access.givenRepository()
    val public = access.givenRepository(EntityVisibility.isPublic)

    assertNotFound(
      mockMvc.deleteAs(access.stranger, sourceUrl(private, givenSource(private.id))),
      "repository ${private.id.uuid} not found",
    )
    assertNotFound(
      mockMvc.deleteAs(access.stranger, sourceUrl(public, givenSource(public.id))),
      "repository ${public.id.uuid} not found",
    )
    verify(sourceUseCase, never()).deleteAllById(any(), any())
  }

  @Test
  fun `deleteSource returns 404 when source missing`() = runTest {
    val private = access.givenRepository()
    val sourceId = SourceId()
    whenever(sourceRepository.findByIdWithActions(eq(sourceId))).thenReturn(null)

    assertNotFound(mockMvc.deleteAs(access.owner, "${sourcesUrl(private)}/${sourceId.uuid}"), "source ${sourceId.uuid} not found")
    verify(sourceUseCase, never()).deleteAllById(any(), any())
  }

  @Test
  fun `deleteSource returns 404 when source belongs to another repository`() = runTest {
    val private = access.givenRepository()
    val foreign = givenSource(RepositoryId())

    assertNotFound(mockMvc.deleteAs(access.owner, sourceUrl(private, foreign)), "source ${foreign.id.uuid} not found")
    verify(sourceUseCase, never()).deleteAllById(any(), any())
  }

  private fun givenSource(repositoryId: RepositoryId): Source {
    val id = SourceId()
    val source = Source(
      id = id,
      title = "Test source",
      repositoryId = repositoryId,
      actions = listOf(FetchAction(sourceId = id, url = "https://example.com")),
    )
    whenever(sourceRepository.findByIdWithActions(eq(id))).thenReturn(source)
    return source
  }

  private fun sourcesUrl(repo: Repository) = "/api/v1/repositories/${repo.id.uuid}/sources"

  private fun sourceUrl(repo: Repository, source: Source) = "${sourcesUrl(repo)}/${source.id.uuid}"

  private companion object {
    const val UPDATE = """{"title":"updated"}"""
    const val CREATE =
      """{"title":"Test source","flow":{"sequence":[{"fetch":{"get":{"url":{"literal":"https://example.com"}}}}]}}"""
  }
}
