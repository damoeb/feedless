package org.migor.feedless.http

import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.never
import org.mockito.kotlin.verifyBlocking
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.EntityVisibility
import org.migor.feedless.NotFoundException
import org.migor.feedless.analytics.Analytics
import org.migor.feedless.api.http.HttpExceptionHandler
import org.migor.feedless.attachment.Attachment
import org.migor.feedless.attachment.AttachmentController
import org.migor.feedless.attachment.AttachmentId
import org.migor.feedless.attachment.AttachmentUseCase
import org.migor.feedless.capability.UserCapability
import org.migor.feedless.common.HttpFetcher
import org.migor.feedless.document.Document
import org.migor.feedless.document.DocumentController
import org.migor.feedless.document.DocumentGuard
import org.migor.feedless.document.DocumentId
import org.migor.feedless.document.DocumentRepository
import org.migor.feedless.document.ReleaseStatus
import org.migor.feedless.feed.FeedController
import org.migor.feedless.feed.FeedService
import org.migor.feedless.feed.exporter.FeedExporter
import org.migor.feedless.feed.exporter.ResponseType
import org.migor.feedless.feed.parser.json.JsonFeed
import org.migor.feedless.group.GroupId
import org.migor.feedless.repository.Repository
import org.migor.feedless.repository.RepositoryGuard
import org.migor.feedless.repository.RepositoryRepository
import org.migor.feedless.session.LazyGrantedAuthority
import org.migor.feedless.source.Source
import org.migor.feedless.source.SourceId
import org.migor.feedless.source.SourceRepository
import org.migor.feedless.user.User
import org.migor.feedless.user.UserGuard
import org.migor.feedless.user.UserId
import org.migor.feedless.user.UserRepository
import org.migor.feedless.userGroup.RoleInGroup
import org.migor.feedless.userGroup.UserGroupAssignment
import org.migor.feedless.userGroup.UserGroupAssignmentRepository
import org.migor.feedless.util.JsonSerializer
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import java.time.LocalDateTime
import java.util.*
import kotlin.time.Duration

/** The anonymous export routes besides /f/: article redirects, attachments and the legacy source feeds. */
@WebMvcTest(controllers = [DocumentController::class, AttachmentController::class, FeedController::class])
@AutoConfigureMockMvc(addFilters = false)
@Import(
  DocumentController::class,
  AttachmentController::class,
  FeedController::class,
  DocumentGuard::class,
  RepositoryGuard::class,
  UserGuard::class,
  HttpExceptionHandler::class,
  AnonymousExportAccessTest.Config::class,
)
@ActiveProfiles(
  "test",
  AppLayer.api,
  AppLayer.service,
  AppProfiles.repository,
  AppProfiles.user,
  AppProfiles.document,
  AppProfiles.attachment,
  AppProfiles.feed,
)
class AnonymousExportAccessTest {

  @TestConfiguration
  class Config {
    @Bean
    fun meterRegistry(): MeterRegistry = SimpleMeterRegistry()
  }

  @Autowired
  private lateinit var mockMvc: MockMvc

  @MockitoBean
  private lateinit var repositoryRepository: RepositoryRepository

  @MockitoBean
  private lateinit var userRepository: UserRepository

  @MockitoBean
  private lateinit var userGroupAssignmentRepository: UserGroupAssignmentRepository

  @MockitoBean
  private lateinit var documentRepository: DocumentRepository

  @MockitoBean
  private lateinit var attachmentUseCase: AttachmentUseCase

  @MockitoBean
  private lateinit var httpFetcher: HttpFetcher

  @MockitoBean
  private lateinit var analytics: Analytics

  @MockitoBean
  private lateinit var feedService: FeedService

  @MockitoBean
  private lateinit var feedExporter: FeedExporter

  @MockitoBean
  private lateinit var sourceRepository: SourceRepository

  private val owner = UserId()
  private val member = UserId()
  private val stranger = UserId()
  private val groupId = GroupId()

  @BeforeEach
  fun setUp() = runBlocking {
    whenever(userRepository.findById(any())).thenReturn(mock<User>())
    whenever(userGroupAssignmentRepository.findAllByUserId(any())).thenReturn(emptyList())
    whenever(userGroupAssignmentRepository.findAllByUserId(eq(member))).thenReturn(
      listOf(UserGroupAssignment(role = RoleInGroup.viewer, userId = member, groupId = groupId)),
    )
    whenever(feedExporter.resolveResponseType(any())).thenReturn(
      ResponseType.atom to { feed: JsonFeed, status: HttpStatus, _: Duration? ->
        ResponseEntity.status(status).body("feed ${feed.title}")
      },
    )
    whenever(feedService.createErrorFeed(any(), any())).thenAnswer {
      JsonFeed().apply { title = "error ${it.getArgument<Throwable>(1).message}" }
    }
    whenever(feedService.getFeed(any(), any(), any())).thenAnswer { JsonFeed().apply { title = "content" } }
    Unit
  }

  @AfterEach
  fun logout() {
    SecurityContextHolder.clearContext()
  }

  // /article/{id} and /a/{id}

  @Test
  fun `an article of a public repository redirects anyone`() {
    val document = givenDocument(givenRepository(EntityVisibility.isPublic))

    assertRedirect(mockMvc.getAnonymous("/article/${document.id.uuid}"), document)
    assertRedirect(mockMvc.getAnonymous("/a/${document.id.uuid}"), document)
  }

  @Test
  fun `an article of a private repository answers anonymous callers like a missing one`() {
    val document = givenDocument(givenRepository())

    assertLikeMissingArticle(mockMvc.getAnonymous("/article/${document.id.uuid}"))
    assertLikeMissingArticle(mockMvc.getAnonymous("/article/${document.id.uuid}?skey=$SHARE_KEY"))
  }

  @Test
  fun `an article of a private repository redirects the owner and group members`() {
    val document = givenDocument(givenRepository())

    loginAs(owner)
    assertRedirect(mockMvc.getAnonymous("/article/${document.id.uuid}"), document)
    loginAs(member)
    assertRedirect(mockMvc.getAnonymous("/article/${document.id.uuid}"), document)
  }

  @Test
  fun `an article of a private repository answers a logged-in stranger like a missing one`() {
    val document = givenDocument(givenRepository())

    loginAs(stranger)
    assertLikeMissingArticle(mockMvc.getAnonymous("/article/${document.id.uuid}"))
  }

  // /attachment/{id}

  @Test
  fun `an attachment of a public repository is served to anyone`() {
    val attachment = givenAttachment(givenDocument(givenRepository(EntityVisibility.isPublic)))

    assertStatus(mockMvc.getAnonymous("/attachment/${attachment.id.uuid}"), 200)
  }

  @Test
  fun `an attachment of a private repository answers anonymous callers like a missing one`() {
    val attachment = givenAttachment(givenDocument(givenRepository()))

    assertLikeMissingAttachment(mockMvc.getAnonymous("/attachment/${attachment.id.uuid}"))
    assertLikeMissingAttachment(mockMvc.getAnonymous("/attachment/${attachment.id.uuid}?skey=$SHARE_KEY"))
  }

  @Test
  fun `an attachment of a private repository is served to the owner and group members only`() {
    val attachment = givenAttachment(givenDocument(givenRepository()))

    loginAs(owner)
    assertStatus(mockMvc.getAnonymous("/attachment/${attachment.id.uuid}"), 200)
    loginAs(member)
    assertStatus(mockMvc.getAnonymous("/attachment/${attachment.id.uuid}"), 200)
    loginAs(stranger)
    assertLikeMissingAttachment(mockMvc.getAnonymous("/attachment/${attachment.id.uuid}"))
  }

  // legacy /feed/{sourceId}, /stream/feed/{sourceId}, /feed:{sourceId}

  @Test
  fun `a legacy source feed of a public repository is served to anyone`() {
    val source = givenSource(givenRepository(EntityVisibility.isPublic))

    assertFeed(mockMvc.getAnonymous("/feed/${source.id.uuid}"), "feed content")
    assertFeed(mockMvc.getAnonymous("/stream/feed/${source.id.uuid}/atom"), "feed content")
  }

  @Test
  fun `a legacy source feed of a private repository answers anonymous callers like a missing source`() {
    val source = givenSource(givenRepository())
    val missing = missingSource()

    val expected = mockMvc.getAnonymous("/feed/${missing.uuid}").response.contentAsString
    assertFeed(mockMvc.getAnonymous("/feed/${source.id.uuid}"), expected)
    assertFeed(mockMvc.getAnonymous("/feed/${source.id.uuid}?skey=wrong-key"), expected)
    assert(expected == "feed error feedId not found") { expected }
  }

  @Test
  fun `a legacy source feed of a private repository is served with its share key`() {
    val source = givenSource(givenRepository())

    assertFeed(mockMvc.getAnonymous("/feed/${source.id.uuid}?skey=$SHARE_KEY"), "feed content")
  }

  @Test
  fun `a legacy source feed of a private repository is served to the owner and group members only`() {
    val source = givenSource(givenRepository())

    loginAs(owner)
    assertFeed(mockMvc.getAnonymous("/feed/${source.id.uuid}"), "feed content")
    loginAs(member)
    assertFeed(mockMvc.getAnonymous("/feed/${source.id.uuid}"), "feed content")
    loginAs(stranger)
    assertFeed(mockMvc.getAnonymous("/feed/${source.id.uuid}"), "feed error feedId not found")
  }

  // /api/web-to-feed with a legacy claim token

  @Test
  fun `web-to-feed checks a legacy token's repository before the cached feed, on every request`() {
    runBlocking {
      whenever(feedService.requireLegacyTokenAccess(eq("private-claim")))
        .thenThrow(NotFoundException("Repository private not found"))
    }

    val result = mockMvc.getAnonymous("/api/web-to-feed?url=https://example.org&link=./a&context=//div&token=private-claim")

    assertFeed(result, "feed error Repository private not found")
    verifyBlocking(feedService, never()) { webToFeed(any(), any(), any(), anyOrNull(), any(), any()) }
  }

  private fun givenRepository(visibility: EntityVisibility = EntityVisibility.isPrivate): Repository {
    val repository = Repository(
      title = "feed",
      visibility = visibility,
      shareKey = SHARE_KEY,
      ownerId = owner,
      groupId = groupId,
    )
    runBlocking { whenever(repositoryRepository.findById(eq(repository.id))).thenReturn(repository) }
    return repository
  }

  private fun givenDocument(repository: Repository): Document {
    val document = Document(
      url = "https://example.org/original",
      text = "text",
      repositoryId = repository.id,
      status = ReleaseStatus.released,
      publishedAt = LocalDateTime.now(),
      contentHash = "",
    )
    runBlocking {
      whenever(documentRepository.findById(eq(document.id))).thenReturn(document)
    }
    return document
  }

  private fun givenAttachment(document: Document): Attachment {
    val data = "audio".toByteArray()
    val attachment = Attachment(mimeType = "audio/mpeg", data = data, hasData = true, documentId = document.id)
    runBlocking {
      whenever(attachmentUseCase.findByIdWithData(any())).thenReturn(Pair(Optional.empty(), null))
      whenever(attachmentUseCase.findByIdWithData(eq(attachment.id))).thenReturn(Pair(Optional.of(attachment), data))
    }
    return attachment
  }

  private fun givenSource(repository: Repository): Source {
    val source = Source(id = SourceId(), title = "source", repositoryId = repository.id)
    whenever(sourceRepository.findById(eq(source.id))).thenReturn(source)
    return source
  }

  // what FeedService.getFeed does for an unknown id
  private fun missingSource(): SourceId {
    val missing = SourceId()
    runBlocking {
      whenever(feedService.getFeed(any(), eq(missing), any())).thenThrow(NotFoundException("feedId not found"))
    }
    return missing
  }

  private fun loginAs(userId: UserId) {
    val authorities = listOf(LazyGrantedAuthority(UserCapability.ID.value, JsonSerializer.toJson(userId)))
    val principal = DefaultOAuth2User(authorities, mapOf("id" to "test"), "id")
    SecurityContextHolder.getContext().authentication = OAuth2AuthenticationToken(principal, authorities, "test")
  }

  private fun assertRedirect(result: MvcResult, document: Document) {
    assertStatus(result, 302)
    assert(result.response.getHeader("Location") == document.url) { result.response.getHeader("Location") }
  }

  private fun assertLikeMissingArticle(result: MvcResult) {
    val missing = mockMvc.getAnonymous("/article/${DocumentId().uuid}")
    assertStatus(result, 404)
    assert(result.response.contentAsString == missing.response.contentAsString) { result.response.contentAsString }
    assert(result.response.getHeader("Location") == null)
  }

  private fun assertLikeMissingAttachment(result: MvcResult) {
    val missing = mockMvc.getAnonymous("/attachment/${AttachmentId().uuid}")
    assertStatus(result, 404)
    assert(result.response.contentAsString == missing.response.contentAsString) { result.response.contentAsString }
  }

  private fun assertFeed(result: MvcResult, expectedBody: String) {
    assertStatus(result, 200)
    assert(result.response.contentAsString == expectedBody) { result.response.contentAsString }
  }

  private companion object {
    const val SHARE_KEY = "s3cr3t-key"
  }
}
