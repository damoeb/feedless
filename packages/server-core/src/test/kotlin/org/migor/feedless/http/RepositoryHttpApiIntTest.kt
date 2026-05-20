package org.migor.feedless.http

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.EntityVisibility
import org.migor.feedless.Vertical
import org.migor.feedless.common.PropertyService
import org.migor.feedless.group.Group
import org.migor.feedless.group.GroupId
import org.migor.feedless.group.GroupRepository
import org.migor.feedless.repository.Repository
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.repository.RepositoryRepository
import org.migor.feedless.repository.RepositoryUseCase
import org.migor.feedless.session.JwtTokenIssuer
import org.migor.feedless.source.SourceRepository
import org.migor.feedless.user.User
import org.migor.feedless.user.UserId
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(controllers = [RepositoriesHttpController::class, SourcesHttpController::class])
@AutoConfigureMockMvc(addFilters = false)
@Import(HttpAuthSupport::class, RepositoryHttpApiIntTest.JwtTestConfig::class)
@ActiveProfiles("test", AppLayer.api, AppLayer.service, AppProfiles.repository, AppProfiles.session)
class RepositoryHttpApiIntTest {

  @TestConfiguration
  class JwtTestConfig {
    @Bean
    fun propertyService(): PropertyService {
      val propertyService = org.mockito.Mockito.mock(PropertyService::class.java)
      `when`(propertyService.jwtSecret).thenReturn("test-secret-key-that-is-long-enough-for-hmac-sha256-algorithm")
      `when`(propertyService.apiGatewayUrl).thenReturn("https://localhost")
      return propertyService
    }

    @Bean
    fun jwtTokenIssuer(propertyService: PropertyService): JwtTokenIssuer {
      return JwtTokenIssuer(propertyService, SimpleMeterRegistry(), "1", "1").also { it.postConstruct() }
    }
  }

  @Autowired
  private lateinit var mockMvc: MockMvc

  @Autowired
  private lateinit var jwtTokenIssuer: JwtTokenIssuer

  @MockitoBean
  private lateinit var repositoryUseCase: RepositoryUseCase

  @MockitoBean
  private lateinit var repositoryRepository: RepositoryRepository

  @MockitoBean
  private lateinit var sourceRepository: SourceRepository

  @MockitoBean
  private lateinit var groupRepository: GroupRepository

  @MockitoBean
  private lateinit var documentRepository: org.migor.feedless.document.DocumentRepository

  private val userId = UserId()
  private val groupId = GroupId()
  private lateinit var token: String

  @BeforeEach
  fun setUp() {
    val user = org.mockito.Mockito.mock(User::class.java)
    `when`(user.id).thenReturn(userId)
    token = jwtTokenIssuer.createJwtForApi(user).tokenValue

    `when`(groupRepository.findAllByOwner(userId)).thenReturn(
      listOf(Group(id = groupId, name = "test", ownerId = userId)),
    )
  }

  @Test
  fun `lists repositories with bearer token`() = runTest {
    val repository = Repository(
      id = RepositoryId(),
      title = "Test feed",
      description = "desc",
      visibility = EntityVisibility.isPrivate,
      ownerId = userId,
      groupId = groupId,
      product = Vertical.rssProxy,
      shareKey = "share-key",
    )
    whenever(repositoryUseCase.findAllByUserId(any(), anyOrNull(), eq(userId))).thenReturn(listOf(repository))
    `when`(sourceRepository.countByRepositoryId(repository.id)).thenReturn(0)
    `when`(sourceRepository.countSourcesWithProblems(repository.id)).thenReturn(0)

    val mvcResult = mockMvc.get("/api/v1/repositories") {
      header("Authentication", "Bearer $token")
      accept = MediaType.APPLICATION_JSON
    }.andReturn()

    if (mvcResult.request.asyncContext != null) {
      mockMvc.perform(asyncDispatch(mvcResult))
        .andExpect(status().isOk)
        .andExpect {
          assertThat(it.response.contentAsString).contains("Test feed")
        }
    } else {
      assertThat(mvcResult.response.status).isEqualTo(200)
      assertThat(mvcResult.response.contentAsString).contains("Test feed")
    }
  }
}
