package org.migor.feedless.repository

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.EntityVisibility
import org.migor.feedless.PostgreSQLExtension
import org.migor.feedless.agent.AgentService
import org.migor.feedless.any
import org.migor.feedless.any2
import org.migor.feedless.capability.CapabilityService
import org.migor.feedless.capability.UnresolvedCapability
import org.migor.feedless.capability.UserCapability
import org.migor.feedless.common.PropertyService
import org.migor.feedless.data.jpa.source.actions.ExtractXpathActionEntity
import org.migor.feedless.data.jpa.source.actions.ScrapeActionDAO
import org.migor.feedless.document.DocumentRepository
import org.migor.feedless.document.DocumentUseCase
import org.migor.feedless.eq
import org.migor.feedless.feature.FeatureName
import org.migor.feedless.feature.FeatureService
import org.migor.feedless.generated.types.DOMElementByXPathInput
import org.migor.feedless.generated.types.DOMExtractInput
import org.migor.feedless.generated.types.HttpFetchInput
import org.migor.feedless.generated.types.HttpGetRequestInput
import org.migor.feedless.generated.types.RepositoryCreateInput
import org.migor.feedless.generated.types.ScrapeActionInput
import org.migor.feedless.generated.types.ScrapeEmit
import org.migor.feedless.generated.types.ScrapeExtractInput
import org.migor.feedless.generated.types.ScrapeFlowInput
import org.migor.feedless.generated.types.SourceInput
import org.migor.feedless.generated.types.StringLiteralOrVariableInput
import org.migor.feedless.group.GroupId
import org.migor.feedless.plan.PlanConstraintsService
import org.migor.feedless.product.ProductRepository
import org.migor.feedless.product.ProductUseCase
import org.migor.feedless.session.RequestContext
import org.migor.feedless.session.SessionService
import org.migor.feedless.source.ExtractEmit
import org.migor.feedless.user.User
import org.migor.feedless.user.UserUseCase
import org.migor.feedless.util.JsonSerializer.toJson
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.migor.feedless.generated.types.Vertical as VerticalDto

@SpringBootTest
@ExtendWith(PostgreSQLExtension::class)
@DirtiesContext
@ActiveProfiles(
  "test",
  "database",
  AppProfiles.repository,
  AppProfiles.source,
  AppProfiles.user,
  AppProfiles.scrape,
  AppLayer.repository,
  AppLayer.service,
)
@MockitoBean(
  types = [
    ProductRepository::class,
    DocumentRepository::class,
    DocumentUseCase::class,
    ProductUseCase::class,
    PropertyService::class,
    InboxService::class,
//  SourceService::class
    AgentService::class,
  ]
)
class RepositoryServiceIntTest {
  @Autowired
  private lateinit var repositoryUseCase: RepositoryUseCase

  @MockitoBean
  private lateinit var featureService: FeatureService

  @Autowired
  private lateinit var scrapeActionDAO: ScrapeActionDAO

  @MockitoBean
  private lateinit var sessionService: SessionService

  @Autowired
  private lateinit var userUseCase: UserUseCase

  @MockitoBean
  private lateinit var planConstraintsService: PlanConstraintsService

  @MockitoBean
  private lateinit var capabilityService: CapabilityService

  private lateinit var user: User


  @BeforeEach
  fun setup() = runTest {
    `when`(featureService.isDisabled(any(FeatureName::class.java), eq(null))).thenReturn(false)

    user = userUseCase.createUser("foo@bar.com")
  }

  @Test
  fun `create repos`() = runTest(context = RequestContext(userId = user.id)) {
    `when`(capabilityService.getCapability(UserCapability.ID))
      .thenReturn(UnresolvedCapability(UserCapability.ID, toJson(user.id)))

    `when`(sessionService.user())
      .thenReturn(user)
    `when`(planConstraintsService.violatesRepositoriesMaxActiveCount(any(GroupId::class.java)))
      .thenReturn(false)
    `when`(planConstraintsService.coerceVisibility(any2(), eq(null)))
      .thenReturn(EntityVisibility.isPublic)

    repositoryUseCase.create(
      listOf(
        RepositoryCreateInput(
          product = VerticalDto.rssProxy,
          sources = listOf(
            SourceInput(
              title = "wef",
              flow = ScrapeFlowInput(
                sequence = listOf(
                  ScrapeActionInput(
                    fetch = HttpFetchInput(
                      get = HttpGetRequestInput(
                        url = StringLiteralOrVariableInput(
                          literal = ""
                        )
                      )
                    )
                  ),
                  ScrapeActionInput(
                    extract = ScrapeExtractInput(
                      fragmentName = "foo",
                      selectorBased = DOMExtractInput(
                        fragmentName = "foo",
                        emit = listOf(ScrapeEmit.text, ScrapeEmit.pixel),
                        xpath = DOMElementByXPathInput("//bar"),
                        uniqueBy = ScrapeEmit.text
                      )
                    )
                  )
                )
              ),
            )
          ),
          title = "foo",
          description = "bar",
          refreshCron = "",
          withShareKey = true
        )
      )
    )

    val actions = scrapeActionDAO.findAll()
    assertThat(actions).hasSize(2)

    val extractAction = actions.get(1) as ExtractXpathActionEntity
    assertThat(extractAction.emitRaw).isEqualTo(arrayOf(ExtractEmit.text.name, ExtractEmit.pixel.name))
  }

}
