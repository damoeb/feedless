package org.migor.feedless.repository

import PostgreSQLExtension
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.actions.ExtractEmit
import org.migor.feedless.actions.ExtractXpathActionEntity
import org.migor.feedless.actions.ScrapeActionDAO
import org.migor.feedless.agent.AgentService
import org.migor.feedless.common.PropertyService
import org.migor.feedless.data.jpa.enums.EntityVisibility
import org.migor.feedless.document.DocumentDAO
import org.migor.feedless.document.DocumentService
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
import org.migor.feedless.generated.types.Vertical
import org.migor.feedless.plan.PlanConstraintsService
import org.migor.feedless.plan.ProductDAO
import org.migor.feedless.plan.ProductService
import org.migor.feedless.session.RequestContext
import org.migor.feedless.session.SessionService
import org.migor.feedless.user.UserEntity
import org.migor.feedless.user.UserId
import org.migor.feedless.user.UserService
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.MockBeans
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles

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
@MockBeans(
  MockBean(ProductDAO::class),
  MockBean(DocumentDAO::class),
  MockBean(DocumentService::class),
  MockBean(ProductService::class),
  MockBean(PropertyService::class),
  MockBean(InboxService::class),
//  MockBean(SourceService::class),
  MockBean(AgentService::class),
)
class RepositoryServiceIntTest {
  @Autowired
  private lateinit var repositoryService: RepositoryService

  @MockBean
  private lateinit var featureService: FeatureService

  @Autowired
  private lateinit var scrapeActionDAO: ScrapeActionDAO

  @MockBean
  private lateinit var sessionService: SessionService

  @Autowired
  private lateinit var userService: UserService

  @MockBean
  private lateinit var planConstraintsService: PlanConstraintsService

  private lateinit var user: UserEntity


  @BeforeEach
  fun setup() = runTest {
    `when`(featureService.isDisabled(any(FeatureName::class.java), eq(null))).thenReturn(false)

    user = userService.createUser("foo@bar.com")
  }

  @Test
  fun `create repos`() = runTest(context = RequestContext(userId = UserId(user.id))) {

    `when`(sessionService.user())
      .thenReturn(user)
    `when`(sessionService.activeProductFromRequest())
      .thenReturn(org.migor.feedless.data.jpa.enums.Vertical.feedless)
    `when`(planConstraintsService.violatesRepositoriesMaxActiveCount(any(UserId::class.java)))
      .thenReturn(false)
    `when`(planConstraintsService.coerceVisibility(eq(null)))
      .thenReturn(EntityVisibility.isPublic)

    repositoryService.create(
      listOf(
        RepositoryCreateInput(
          product = Vertical.rssProxy,
          sources = listOf(
            SourceInput(
              title = "wef",
              flow = ScrapeFlowInput(
                sequence = listOf(
                  ScrapeActionInput(
                    fetch = HttpFetchInput(get = HttpGetRequestInput(url = StringLiteralOrVariableInput(literal = "")))
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
