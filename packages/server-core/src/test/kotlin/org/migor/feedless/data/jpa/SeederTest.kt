package org.migor.feedless.data.jpa

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.any2
import org.migor.feedless.common.PropertyService
import org.migor.feedless.document.DocumentDAO
import org.migor.feedless.eq
import org.migor.feedless.feature.FeatureGroupDAO
import org.migor.feedless.feature.FeatureService
import org.migor.feedless.feed.StandaloneFeedService
import org.migor.feedless.group.GroupDAO
import org.migor.feedless.group.GroupEntity
import org.migor.feedless.group.UserGroupAssignmentDAO
import org.migor.feedless.plan.PricedProductDAO
import org.migor.feedless.plan.ProductDAO
import org.migor.feedless.repository.RepositoryDAO
import org.migor.feedless.secrets.UserSecretDAO
import org.migor.feedless.user.UserDAO
import org.migor.feedless.user.UserEntity
import org.mockito.Mockito
import org.mockito.Mockito.argThat
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.springframework.core.env.Environment
import java.util.*

class SeederTest {

  private lateinit var featureGroupDAO: FeatureGroupDAO
  private lateinit var featureService: FeatureService
  private lateinit var documentDAO: DocumentDAO
  private lateinit var environment: Environment
  private lateinit var propertyService: PropertyService
  private lateinit var productDAO: ProductDAO
  private lateinit var pricedProductDAO: PricedProductDAO
  private lateinit var userSecretDAO: UserSecretDAO
  private lateinit var repositoryDAO: RepositoryDAO
  private lateinit var standaloneFeedService: StandaloneFeedService
  private lateinit var userDAO: UserDAO
  private lateinit var groupDAO: GroupDAO
  private lateinit var userGroupAssignmentDAO: UserGroupAssignmentDAO
  private lateinit var seeder: Seeder

  @BeforeEach
  fun setUp() {
    featureGroupDAO = Mockito.mock(FeatureGroupDAO::class.java)
    featureService = Mockito.mock(FeatureService::class.java)
    documentDAO = Mockito.mock(DocumentDAO::class.java)
    environment = Mockito.mock(Environment::class.java)
    propertyService = Mockito.mock(PropertyService::class.java)
    productDAO = Mockito.mock(ProductDAO::class.java)
    pricedProductDAO = Mockito.mock(PricedProductDAO::class.java)
    userSecretDAO = Mockito.mock(UserSecretDAO::class.java)
    repositoryDAO = Mockito.mock(RepositoryDAO::class.java)
    standaloneFeedService = Mockito.mock(StandaloneFeedService::class.java)
    groupDAO = Mockito.mock(GroupDAO::class.java)
    userGroupAssignmentDAO = Mockito.mock(UserGroupAssignmentDAO::class.java)
    userDAO = Mockito.mock(UserDAO::class.java)

    seeder = Seeder(
      featureGroupDAO,
      featureService,
      documentDAO,
      environment,
      propertyService,
      productDAO,
      pricedProductDAO,
      userSecretDAO,
      repositoryDAO,
      standaloneFeedService,
      userDAO,
      groupDAO,
      userGroupAssignmentDAO
    )

    Mockito.`when`(propertyService.rootEmail).thenReturn("admin@foo")
    Mockito.`when`(propertyService.anonymousEmail).thenReturn("anon@foo")
    Mockito.`when`(propertyService.rootSecretKey).thenReturn("aSecretSecret")
    Mockito.`when`(userDAO.saveAndFlush(any2())).thenAnswer { it.arguments[0] }
    Mockito.`when`(repositoryDAO.save(any2())).thenAnswer { it.arguments[0] }
    Mockito.`when`(featureGroupDAO.save(any2())).thenAnswer { it.arguments[0] }
    Mockito.`when`(standaloneFeedService.getRepoTitleForStandaloneFeedNotifications()).thenReturn("opsops")


    Mockito.`when`(userDAO.save(any2())).thenAnswer { it.arguments[0] }
    Mockito.`when`(groupDAO.save(any2())).thenAnswer { it.arguments[0] }
    Mockito.`when`(userSecretDAO.save(any2())).thenAnswer { it.arguments[0] }
  }

  @Test
  fun `given root user does not exist, will seed one with key`() {
    Mockito.`when`(userSecretDAO.existsByValueAndOwnerId(any2(), any2())).thenReturn(false)

    seeder.onInit()

    Mockito.verify(userDAO, times(1)).save(argThat { it.admin })
  }

  @Test
  fun `given root user exists, won't do anything`() {
    val root = mock(UserEntity::class.java)
    Mockito.`when`(root.id).thenReturn(UUID.randomUUID())
    Mockito.`when`(root.email).thenReturn("admin@foo")
    Mockito.`when`(userDAO.findFirstByAdminIsTrue()).thenReturn(root)
    Mockito.`when`(userDAO.findByEmail(eq("anon@foo"))).thenReturn(mock(UserEntity::class.java))
    Mockito.`when`(userSecretDAO.existsByValueAndOwnerId(any2(), any2())).thenReturn(true)

    seeder.onInit()

    Mockito.verify(userDAO, times(0)).save(argThat { !it.admin })
  }

  @Test
  fun `given admin group does not exist, will seed one`() {

    seeder.onInit()

    Mockito.verify(groupDAO, times(1)).save(any2())
//    Mockito.verify(userGroupAssignmentDAO, times(1)).save(any2())
  }

  @Test
  fun `given admin group exists, won't do anything`() {
    val adminGroup = mock(GroupEntity::class.java)
    Mockito.`when`(adminGroup.id).thenReturn(UUID.randomUUID())
    Mockito.`when`(groupDAO.findByName(any2())).thenReturn(adminGroup)

    seeder.onInit()

    Mockito.verify(groupDAO, times(0)).save(any2())
//    Mockito.verify(userGroupAssignmentDAO, times(0)).save(any2())
  }
}
