package org.migor.feedless.document

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.EntityVisibility
import org.mapstruct.factory.Mappers
import org.migor.feedless.NotFoundException
import org.migor.feedless.api.DtoMapperFacade
import org.migor.feedless.api.mapper.DocumentMapper
import org.migor.feedless.api.mapper.ScrapeResponseMapper
import org.migor.feedless.api.mapper.UserSecretMapper
import org.migor.feedless.capability.UserCapability
import org.migor.feedless.common.AppConfig
import org.migor.feedless.generated.types.Cursor
import org.migor.feedless.generated.types.RecordsInput
import org.migor.feedless.generated.types.RecordsWhereInput
import org.migor.feedless.generated.types.RepositoryUniqueWhereInput
import org.migor.feedless.group.GroupId
import org.migor.feedless.message.Notifications
import org.migor.feedless.pipeline.PipelinePlugins
import org.migor.feedless.pipelineJob.DocumentPipelineJobRepository
import org.migor.feedless.plan.PlanConstraintsService
import org.migor.feedless.repository.Repository
import org.migor.feedless.repository.RepositoryGuard
import org.migor.feedless.repository.RepositoryRepository
import org.migor.feedless.repository.RepositoryUseCase
import org.migor.feedless.session.LazyGrantedAuthority
import org.migor.feedless.user.User
import org.migor.feedless.user.UserGuard
import org.migor.feedless.user.UserId
import org.migor.feedless.user.UserRepository
import org.migor.feedless.userGroup.RoleInGroup
import org.migor.feedless.userGroup.UserGroupAssignment
import org.migor.feedless.userGroup.UserGroupAssignmentRepository
import org.migor.feedless.util.JsonSerializer
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import java.time.LocalDateTime

/** `records` through the real use case and guards: a private repository's records are for owner and members only. */
class DocumentResolverAccessTest {

  private val owner = UserId()
  private val member = UserId()
  private val groupId = GroupId()
  private val repositoryRepository = mock<RepositoryRepository>()
  private val documentRepository = mock<DocumentRepository>()
  private val userRepository = mock<UserRepository>()
  private val userGroupAssignmentRepository = mock<UserGroupAssignmentRepository>()
  private val repositoryUseCase = mock<RepositoryUseCase>()
  private val appConfig = mock<AppConfig>()
  private val repositoryGuard = RepositoryGuard(repositoryRepository, UserGuard(userRepository), userGroupAssignmentRepository)
  private val documentGuard = DocumentGuard(documentRepository, repositoryGuard)
  private val documentUseCase = DocumentUseCase(
    documentRepository,
    repositoryRepository,
    mock<PlanConstraintsService>(),
    mock<DocumentPipelineJobRepository>(),
    mock<PipelinePlugins>(),
    mock<Notifications>(),
    appConfig,
    documentGuard,
    repositoryGuard,
  )
  private val resolver = DocumentResolver(repositoryUseCase, appConfig, documentUseCase, documentGuard)
  private val repository = Repository(
    title = "private feed",
    visibility = EntityVisibility.isPrivate,
    ownerId = owner,
    groupId = groupId,
  )
  private val document = Document(
    url = "https://example.org/a",
    text = "text",
    repositoryId = repository.id,
    status = ReleaseStatus.released,
    publishedAt = LocalDateTime.now(),
    contentHash = "",
  )

  @BeforeEach
  fun setUp() = runTest {
    // Record.toDto goes through the MapStruct facade Spring would build
    DtoMapperFacade(
      Mappers.getMapper(DocumentMapper::class.java),
      Mappers.getMapper(UserSecretMapper::class.java),
      Mappers.getMapper(ScrapeResponseMapper::class.java),
    )
    whenever(userRepository.findById(any())).thenReturn(mock<User>())
    whenever(userGroupAssignmentRepository.findAllByUserId(any())).thenReturn(emptyList())
    whenever(userGroupAssignmentRepository.findAllByUserId(eq(member))).thenReturn(
      listOf(UserGroupAssignment(role = RoleInGroup.viewer, userId = member, groupId = groupId)),
    )
    whenever(repositoryRepository.findById(eq(repository.id))).thenReturn(repository)
    whenever(repositoryUseCase.findById(eq(repository.id))).thenReturn(repository)
    whenever(documentRepository.findAllFiltered(any(), anyOrNull(), anyOrNull(), any(), any(), any()))
      .thenReturn(listOf(document))
    whenever(appConfig.apiGatewayUrl).thenReturn("http://localhost:8080")
    whenever(appConfig.appHost).thenReturn("http://localhost:4200")
  }

  @AfterEach
  fun logout() {
    SecurityContextHolder.clearContext()
  }

  @Test
  fun `the owner reads the records of a private repository`() = runTest {
    loginAs(owner)
    assertThat(records().map { it.id }).containsExactly(document.id.uuid.toString())
  }

  @Test
  fun `a member of the owning group reads the records of a private repository`() = runTest {
    loginAs(member)
    assertThat(records().map { it.id }).containsExactly(document.id.uuid.toString())
  }

  @Test
  fun `a logged-in stranger cannot read the records of a private repository`() = runTest {
    loginAs(UserId())
    assertThat(runCatching { records() }.exceptionOrNull()).isInstanceOf(NotFoundException::class.java)
  }

  @Test
  fun `an anonymous caller cannot read the records of a private repository`() = runTest {
    assertThat(runCatching { records() }.exceptionOrNull()).isInstanceOf(NotFoundException::class.java)
  }

  private suspend fun records() = resolver.records(
    mock(),
    RecordsInput(
      cursor = Cursor(page = 0, pageSize = 10),
      where = RecordsWhereInput(repository = RepositoryUniqueWhereInput(id = repository.id.uuid.toString())),
    ),
  )

  private fun loginAs(userId: UserId) {
    val authorities = listOf(LazyGrantedAuthority(UserCapability.ID.value, JsonSerializer.toJson(userId)))
    val principal = DefaultOAuth2User(authorities, mapOf("id" to "test"), "id")
    SecurityContextHolder.getContext().authentication = OAuth2AuthenticationToken(principal, authorities, "test")
  }
}
