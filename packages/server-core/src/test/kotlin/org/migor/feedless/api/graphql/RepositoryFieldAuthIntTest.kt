package org.migor.feedless.api.graphql

import com.netflix.graphql.dgs.client.GraphQLResponse
import com.netflix.graphql.dgs.client.MonoGraphQLClient
import kotlinx.coroutines.future.await
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.DisableDatabaseConfiguration
import org.migor.feedless.EntityVisibility
import org.migor.feedless.Mother.randomUser
import org.migor.feedless.analytics.Analytics
import org.migor.feedless.api.ApiParams
import org.migor.feedless.capability.MdcKeys
import org.migor.feedless.capability.UserCapability
import org.migor.feedless.document.DocumentFrequency
import org.migor.feedless.document.DocumentRepository
import org.migor.feedless.group.GroupId
import org.migor.feedless.group.GroupRepository
import org.migor.feedless.message.Notifications
import org.migor.feedless.payment.PaymentUseCase
import org.migor.feedless.pipeline.PipelinePlugins
import org.migor.feedless.pipelineJob.DocumentPipelineJobRepository
import org.migor.feedless.plan.PlanConstraintsService
import org.migor.feedless.repository.HarvestService
import org.migor.feedless.repository.InboxService
import org.migor.feedless.repository.Repository
import org.migor.feedless.repository.RepositoryRepository
import org.migor.feedless.repository.RepositoryUseCase
import org.migor.feedless.session.JwtTokenIssuer
import org.migor.feedless.source.Source
import org.migor.feedless.source.SourceRepository
import org.migor.feedless.user.User
import org.migor.feedless.user.UserGuard
import org.migor.feedless.user.UserRepository
import org.migor.feedless.userGroup.UserGroupAssignmentRepository
import org.migor.feedless.userSecret.UserSecretRepository
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.web.reactive.function.client.WebClient
import java.util.concurrent.CopyOnWriteArrayList

/** Field resolvers run on whichever thread completed their parent, so they must still see the request's caller. */
@SpringBootTest(
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
  properties = [
    "app.rootEmail=$rootEmail",
    "app.rootSecretKey=$rootSecretKey",
    "app.apiGatewayUrl=https://localhost",
    "app.actuatorPassword=s3cr3t",
  ],
)
@ActiveProfiles(
  "test",
  AppLayer.api,
  AppLayer.service,
  AppProfiles.properties,
  AppLayer.security,
  AppProfiles.authRoot,
  AppProfiles.session,
  AppProfiles.repository,
  AppProfiles.document,
)
@MockitoBean(
  types = [
    ServerConfigResolver::class,
    UserGroupAssignmentRepository::class,
    GroupRepository::class,
    UserGuard::class,
    UserSecretRepository::class,
    OAuth2AuthorizedClientService::class,
    RepositoryUseCase::class,
    HarvestService::class,
    InboxService::class,
    PlanConstraintsService::class,
    DocumentPipelineJobRepository::class,
    PipelinePlugins::class,
    Notifications::class,
    PaymentUseCase::class,
    Analytics::class,
  ]
)
@Import(DisableDatabaseConfiguration::class)
class RepositoryFieldAuthIntTest {

  @LocalServerPort
  private var port: Int = 0

  @Autowired
  lateinit var jwtTokenIssuer: JwtTokenIssuer

  @MockitoBean
  lateinit var userRepository: UserRepository

  @MockitoBean
  lateinit var repositoryRepository: RepositoryRepository

  @MockitoBean
  lateinit var sourceRepository: SourceRepository

  @MockitoBean
  lateinit var documentRepository: DocumentRepository

  private val corrId = "fieldAuthCorr1"
  private val owner = randomUser()
  private val stranger = randomUser()
  private val repository = Repository(
    title = "private",
    visibility = EntityVisibility.isPrivate,
    ownerId = owner.id,
    groupId = GroupId(),
  )
  private val guardThreads = CopyOnWriteArrayList<String>()
  private val fieldCorrIds = CopyOnWriteArrayList<String?>()

  @BeforeEach
  fun setUp() {
    `when`(userRepository.findById(eq(owner.id))).thenReturn(owner)
    `when`(userRepository.findById(eq(stranger.id))).thenReturn(stranger)
    `when`(repositoryRepository.findById(eq(repository.id))).thenAnswer {
      guardThreads.add(Thread.currentThread().name)
      repository
    }
    `when`(sourceRepository.countByRepositoryId(eq(repository.id))).thenAnswer {
      fieldCorrIds.add(MDC.get(MdcKeys.CORR_ID))
      3L
    }
    `when`(sourceRepository.findAllByRepositoryIdFiltered(eq(repository.id), any(), anyOrNull(), anyOrNull()))
      .thenReturn(listOf(Source(title = "source", repositoryId = repository.id)))
    `when`(documentRepository.getRecordFrequency(any(), any())).thenReturn(listOf(DocumentFrequency(count = 2, group = 1L)))
  }

  @Test
  fun `field resolvers of a private repository see its logged-in owner`() = runTest {
    val response = queryRepositoryAs(owner)

    assertThat(response.errors).isEmpty()
    // requireRead hops to an IO worker, so the field resolvers start there, not on the request thread
    assertThat(guardThreads).anyMatch { it.startsWith("DefaultDispatcher-worker") }
    assertThat(response.extractValue<List<Any>?>("data.repository.frequency")).isNotNull.isNotEmpty
    assertThat(response.extractValue<Int>("data.repository.sourcesCount")).isEqualTo(3)
    assertThat(response.extractValue<List<Any>?>("data.repository.sources")).isNotNull.isNotEmpty
    assertThat(fieldCorrIds).containsOnly(corrId)
  }

  @Test
  fun `a different logged-in user gets not found`() = runTest {
    val response = queryRepositoryAs(stranger)

    assertThat(response.errors).isNotEmpty
    assertThat(response.errors.map { it.message }).anyMatch { it.contains("not found") }
    assertThat(response.data["repository"]).isNull()
  }

  private suspend fun queryRepositoryAs(user: User): GraphQLResponse {
    val jwt = jwtTokenIssuer.createJwtForCapabilities(listOf(UserCapability(user.id)))
    val client = MonoGraphQLClient.createWithWebClient(
      WebClient.builder()
        .baseUrl("http://localhost:$port/graphql")
        .defaultHeader("Authorization", "Bearer ${jwt.tokenValue}")
        .defaultHeader(ApiParams.corrId, corrId)
        .build()
    )
    val query = """
      query {
        repository(data: {where: {id: "${repository.id.uuid}"}}) {
          id
          frequency(groupBy: createdAt) { count group }
          sourcesCount
          sources(cursor: {page: 0, pageSize: 10}) { id }
        }
      }
    """.trimIndent()
    return client.reactiveExecuteQuery(query).toFuture().await()!!
  }
}
