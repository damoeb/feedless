package org.migor.feedless.repository

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.EntityVisibility
import org.migor.feedless.PageableRequest
import org.migor.feedless.PostgreSQLExtension
import org.migor.feedless.agent.AgentService
import org.migor.feedless.actions.ExtractXpathAction
import org.migor.feedless.actions.FetchAction
import org.migor.feedless.any
import org.migor.feedless.any2
import org.migor.feedless.capability.RequestContext
import org.migor.feedless.common.PropertyService
import org.migor.feedless.data.jpa.source.actions.ExtractXpathActionEntity
import org.migor.feedless.data.jpa.source.actions.ScrapeActionDAO
import org.migor.feedless.document.DocumentRepository
import org.migor.feedless.document.DocumentUseCase
import org.migor.feedless.eq
import org.migor.feedless.feature.FeatureName
import org.migor.feedless.feature.FeatureService
import org.migor.feedless.group.Group
import org.migor.feedless.group.GroupId
import org.migor.feedless.group.GroupRepository
import org.migor.feedless.order.OrderRepository
import org.migor.feedless.pipeline.SourcePipelineService
import org.migor.feedless.plan.PlanConstraintsService
import org.migor.feedless.product.ProductRepository
import org.migor.feedless.product.ProductUseCase
import org.migor.feedless.repository.RepositoryCreate
import org.migor.feedless.session.StatelessAuthService
import org.migor.feedless.source.ExtractEmit
import org.migor.feedless.source.Source
import org.migor.feedless.source.SourceId
import org.migor.feedless.user.User
import org.migor.feedless.user.UserUseCase
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.migor.feedless.Vertical

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
    StatelessAuthService::class,
    OrderRepository::class,
    AgentService::class,
    SourcePipelineService::class,
  ]
)
class RepositoryUseCaseIntTest {
  @Autowired
  private lateinit var repositoryUseCase: RepositoryUseCase

  @MockitoBean
  private lateinit var featureService: FeatureService

  @Autowired
  private lateinit var scrapeActionDAO: ScrapeActionDAO

  @Autowired
  private lateinit var userUseCase: UserUseCase

  @Autowired
  private lateinit var groupRepository: GroupRepository

  @MockitoBean
  private lateinit var planConstraintsService: PlanConstraintsService

  private lateinit var user: User
  private lateinit var group: Group


  @BeforeEach
  fun setup() = runTest {
    `when`(featureService.isDisabled(any(FeatureName::class.java), eq(null))).thenReturn(false)

    // Data is not reset between tests in this class (shared Testcontainers Postgres), so each
    // test needs its own user to avoid "user already exists".
    user = userUseCase.createUser("foo+${java.util.UUID.randomUUID()}@bar.com")
    group = groupRepository.findAllByOwner(user.id).single()
  }

  @Test
  fun `create repos`() = runTest(context = RequestContext(groupId = group.id, userId = user.id)) {
    `when`(planConstraintsService.violatesRepositoriesMaxActiveCount(any(GroupId::class.java)))
      .thenReturn(false)
    `when`(planConstraintsService.coerceVisibility(any2(), eq(null)))
      .thenReturn(EntityVisibility.isPublic)

    val sourceId = SourceId()
    repositoryUseCase.create(
      listOf(
        RepositoryCreate(
          product = Vertical.rssProxy,
          sources = listOf(
            Source(
              title = "wef",
              actions = listOf(
                FetchAction(sourceId = sourceId, url = ""),
                ExtractXpathAction(
                  sourceId = sourceId,
                  fragmentName = "foo",
                  xpath = "//bar",
                  emit = arrayOf(ExtractEmit.text, ExtractEmit.pixel),
                  uniqueBy = ExtractEmit.text,
                ),
              ),
            )
          ),
          title = "foo",
          description = "bar",
          refreshCron = "",
        )
      )
    )

    val actions = scrapeActionDAO.findAll()
    assertThat(actions).hasSize(2)

    val extractAction = actions.get(1) as ExtractXpathActionEntity
    assertThat(extractAction.emitRaw).isEqualTo(arrayOf(ExtractEmit.text.name, ExtractEmit.pixel.name))
  }

  @Test
  fun `countAllByUserId counts with the same filters as findAllByUserId`() =
    runTest(context = RequestContext(groupId = group.id, userId = user.id)) {
      `when`(planConstraintsService.violatesRepositoriesMaxActiveCount(any(GroupId::class.java)))
        .thenReturn(false)
      // Private, so this doesn't leak into other users' "public" counts in this shared-DB test class.
      `when`(planConstraintsService.coerceVisibility(any2(), eq(null)))
        .thenReturn(EntityVisibility.isPrivate)

      // createUser already created an inbox repository — count relative to that baseline.
      val baseline = repositoryUseCase.countAllByUserId(null, user.id)

      repositoryUseCase.create(
        listOf(
          RepositoryCreate(product = Vertical.rssProxy, title = "r1", description = "d", refreshCron = ""),
          RepositoryCreate(product = Vertical.visualDiff, title = "r2", description = "d", refreshCron = ""),
        )
      )

      val totalCount = repositoryUseCase.countAllByUserId(null, user.id)
      assertThat(totalCount).isEqualTo(baseline + 2)
      assertThat(repositoryUseCase.findAllByUserId(PageableRequest(pageNumber = 0, pageSize = 10), null, user.id))
        .hasSize(totalCount)

      val filteredCount = repositoryUseCase.countAllByUserId(
        RepositoriesFilter(product = VerticalFilter(eq = Vertical.rssProxy)),
        user.id,
      )
      assertThat(filteredCount).isEqualTo(1)
    }

  @Test
  fun `findAllByUserId pages without skipping or repeating rows, mirroring listRepositories' ask-for-one-extra pattern`() =
    runTest(context = RequestContext(groupId = group.id, userId = user.id)) {
      `when`(planConstraintsService.violatesRepositoriesMaxActiveCount(any(GroupId::class.java)))
        .thenReturn(false)
      `when`(planConstraintsService.coerceVisibility(any2(), eq(null)))
        .thenReturn(EntityVisibility.isPrivate)

      repositoryUseCase.create(
        (1..5).map {
          RepositoryCreate(product = Vertical.rssProxy, title = "page-walk-$it", description = "d", refreshCron = "")
        }
      )

      val totalCount = repositoryUseCase.countAllByUserId(null, user.id)
      // The canonical, un-paginated order this walk must reproduce exactly.
      val all = repositoryUseCase.findAllByUserId(PageableRequest(0, totalCount), null, user.id)

      // Mirrors RepositoryHttpController.listRepositories: ask for one more than the page holds.
      var page = 0
      val returned = mutableListOf<RepositoryId>()
      while (true) {
        val fetched = repositoryUseCase.findAllByUserId(PageableRequest.withExtraForHasMore(page, 2), null, user.id)
        val hasMore = fetched.size > 2
        returned.addAll(fetched.take(2).map { it.id })
        if (!hasMore) break
        page++
      }

      assertThat(returned).containsExactlyElementsOf(all.map { it.id })
    }

}
