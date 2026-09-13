package org.migor.feedless.http

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.EntityVisibility
import org.migor.feedless.api.http.HttpExceptionHandler
import org.migor.feedless.common.AppConfig
import org.migor.feedless.config.CacheNames
import org.migor.feedless.document.DocumentQueryParser
import org.migor.feedless.document.DocumentUseCase
import org.migor.feedless.document.DocumentsFilter
import org.migor.feedless.feed.exporter.FeedExporter
import org.migor.feedless.feed.parser.json.JsonFeed
import org.migor.feedless.group.GroupId
import org.migor.feedless.plan.PlanConstraintsService
import org.migor.feedless.repository.Repository
import org.migor.feedless.repository.RepositoryController
import org.migor.feedless.repository.RepositoryGuard
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.repository.RepositoryRepository
import org.migor.feedless.repository.RepositoryUseCase
import org.migor.feedless.source.SourceUseCase
import org.migor.feedless.user.User
import org.migor.feedless.user.UserGuard
import org.migor.feedless.user.UserId
import org.migor.feedless.user.UserRepository
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doSuspendableAnswer
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub
import org.mockito.kotlin.times
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.concurrent.ConcurrentMapCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult

@WebMvcTest(controllers = [RepositoryController::class])
@AutoConfigureMockMvc(addFilters = false)
@Import(
  RepositoryController::class,
  RepositoryUseCase::class,
  RepositoryGuard::class,
  UserGuard::class,
  HttpExceptionHandler::class,
  RequestContextBridge::class,
  RepositoryFeedControllerTest.FeedCacheConfig::class,
)
@ActiveProfiles("test", AppLayer.api, AppLayer.service, AppProfiles.repository, AppProfiles.user)
class RepositoryFeedControllerTest {

  // the feed cache is on, as in saas, so a cache hit would skip a read check that lives inside it
  // class proxies like Boot's AopAutoConfiguration, which this slice does not load
  @TestConfiguration
  @EnableCaching(proxyTargetClass = true)
  class FeedCacheConfig {
    @Bean
    fun cacheManager(): CacheManager = ConcurrentMapCacheManager(CacheNames.FEED_SHORT_TTL)

    @Bean
    fun meterRegistry(): MeterRegistry = SimpleMeterRegistry()

    @Bean
    fun appConfig(): AppConfig = object : AppConfig {
      override val apiGatewayUrl = "http://localhost:8080"
      override val appHost = "http://localhost:4200"
    }
  }

  @Autowired
  private lateinit var mockMvc: MockMvc

  @Autowired
  private lateinit var repositoryGuard: RepositoryGuard

  @MockitoBean
  private lateinit var repositoryRepository: RepositoryRepository

  @MockitoBean
  private lateinit var userRepository: UserRepository

  @MockitoBean
  private lateinit var documentUseCase: DocumentUseCase

  @MockitoBean
  private lateinit var planConstraintsService: PlanConstraintsService

  @MockitoBean
  private lateinit var sourceUseCase: SourceUseCase

  @MockitoBean
  private lateinit var feedExporter: FeedExporter

  @MockitoBean
  private lateinit var documentQueryParser: DocumentQueryParser

  private val owner = UserId()

  @BeforeEach
  fun setUp() = runBlocking {
    whenever(userRepository.findById(any())).thenReturn(mock<User>())
    // stands in for DocumentUseCase, which runs the same read check before it loads documents
    documentUseCase.stub {
      onBlocking { findAllByRepositoryId(any(), anyOrNull(), anyOrNull(), any(), any(), any()) } doSuspendableAnswer {
        repositoryGuard.requireRead(it.getArgument(0))
        emptyList()
      }
    }
    whenever(feedExporter.to(any(), any(), any(), anyOrNull())).thenAnswer {
      val feed = it.getArgument<JsonFeed>(2)
      ResponseEntity.ok("feed ${feed.id} page ${feed.page}")
    }
    Unit
  }

  @Test
  fun `a private feed without a key answers 404`() {
    val repository = givenRepository()

    assertDenied(mockMvc.getAnonymous(feedUrl(repository)), repository)
  }

  @Test
  fun `a private feed with its share key answers 200`() {
    val repository = givenRepository()

    val result = mockMvc.getAnonymous(feedUrl(repository, "skey=$SHARE_KEY"))

    assertStatus(result, 200)
    assert(result.response.contentAsString.contains("repository:${repository.id}")) { result.response.contentAsString }
  }

  @Test
  fun `a private feed with a wrong key answers exactly like no key`() {
    val repository = givenRepository()

    val withoutKey = mockMvc.getAnonymous(feedUrl(repository))
    val wrongKey = mockMvc.getAnonymous(feedUrl(repository, "skey=wrong-key"))

    assertDenied(wrongKey, repository)
    assert(messageOf(wrongKey) == messageOf(withoutKey)) { "${messageOf(wrongKey)} vs ${messageOf(withoutKey)}" }
  }

  @Test
  fun `a keyed request does not open the cached feed to requests without the key`() {
    val repository = givenRepository()

    assertStatus(mockMvc.getAnonymous(feedUrl(repository, "skey=$SHARE_KEY")), 200)
    assertStatus(mockMvc.getAnonymous(feedUrl(repository, "skey=$SHARE_KEY")), 200)
    verifyBlocking(documentUseCase, times(1)) { findAllByRepositoryId(any(), anyOrNull(), anyOrNull(), any(), any(), any()) }

    assertDenied(mockMvc.getAnonymous(feedUrl(repository)), repository)
    assertDenied(mockMvc.getAnonymous(feedUrl(repository, "skey=wrong-key")), repository)
  }

  @Test
  fun `an owner request does not open the cached feed to anonymous requests`() {
    val repository = givenRepository()

    assertStatus(mockMvc.getAs(owner, feedUrl(repository)), 200)

    assertDenied(mockMvc.getAnonymous(feedUrl(repository)), repository)
  }

  @Test
  fun `a public feed ignores skey`() {
    val repository = givenRepository(EntityVisibility.isPublic)

    assertStatus(mockMvc.getAnonymous(feedUrl(repository)), 200)
    assertStatus(mockMvc.getAnonymous(feedUrl(repository, "skey=wrong-key")), 200)
    assertStatus(mockMvc.getAnonymous(feedUrl(repository, "skey=$SHARE_KEY")), 200)
  }

  @Test
  fun `the feed cache tells pages apart`() {
    val repository = givenRepository(EntityVisibility.isPublic)

    val first = mockMvc.getAnonymous(feedUrl(repository, "page=0"))
    val second = mockMvc.getAnonymous(feedUrl(repository, "page=1"))

    assert(first.response.contentAsString.endsWith("page 0")) { first.response.contentAsString }
    assert(second.response.contentAsString.endsWith("page 1")) { second.response.contentAsString }
  }

  @Test
  fun `the feed cache tells filters apart`() {
    val repository = givenRepository(EntityVisibility.isPublic)
    whenever(documentQueryParser.parseFilter(eq("a"))).thenReturn(DocumentsFilter(repository = repository.id))

    assertStatus(mockMvc.getAnonymous(feedUrl(repository, "where=a")), 200)
    assertStatus(mockMvc.getAnonymous(feedUrl(repository)), 200)
    verifyBlocking(documentUseCase, times(2)) { findAllByRepositoryId(any(), anyOrNull(), anyOrNull(), any(), any(), any()) }
  }

  private fun givenRepository(visibility: EntityVisibility = EntityVisibility.isPrivate): Repository {
    val repository = Repository(
      title = "feed",
      visibility = visibility,
      shareKey = SHARE_KEY,
      ownerId = owner,
      groupId = GroupId(),
    )
    runBlocking { whenever(repositoryRepository.findById(eq(repository.id))).thenReturn(repository) }
    return repository
  }

  private fun feedUrl(repository: Repository, query: String? = null): String =
    "/f/${repository.id.uuid}/atom" + (query?.let { "?$it" } ?: "")

  private fun assertDenied(result: MvcResult, repository: Repository) {
    assertStatus(result, 404)
    val message = messageOf(result)
    assert(message == "Repository ${RepositoryId(repository.id.uuid)} is private, you are not logged in") { message }
  }

  private fun messageOf(result: MvcResult): String =
    Regex("\"message\":\"([^\"]*)\"").find(result.response.contentAsString)?.groupValues?.get(1)
      ?: result.response.contentAsString

  private companion object {
    const val SHARE_KEY = "s3cr3t-key"
  }
}
