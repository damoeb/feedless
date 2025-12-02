package org.migor.feedless.data.jpa

import kotlinx.coroutines.test.runTest
import net.jqwik.api.Disabled
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.any2
import org.migor.feedless.argThat
import org.migor.feedless.common.PropertyService
import org.migor.feedless.data.Seeder
import org.migor.feedless.eq
import org.migor.feedless.feature.FeatureGroupRepository
import org.migor.feedless.feature.FeatureService
import org.migor.feedless.group.Group
import org.migor.feedless.group.GroupId
import org.migor.feedless.group.GroupRepository
import org.migor.feedless.pricedProduct.PricedProductRepository
import org.migor.feedless.product.ProductRepository
import org.migor.feedless.user.User
import org.migor.feedless.user.UserId
import org.migor.feedless.user.UserRepository
import org.migor.feedless.userGroup.UserGroupAssignmentRepository
import org.migor.feedless.userSecret.UserSecretRepository
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.core.env.Environment

class SeederTest {

  private lateinit var featureGroupRepository: FeatureGroupRepository
  private lateinit var featureService: FeatureService
  private lateinit var environment: Environment
  private lateinit var propertyService: PropertyService
  private lateinit var productRepository: ProductRepository
  private lateinit var pricedProductRepository: PricedProductRepository
  private lateinit var userSecretRepository: UserSecretRepository
  private lateinit var userRepository: UserRepository
  private lateinit var groupRepository: GroupRepository
  private lateinit var userGroupAssignmentRepository: UserGroupAssignmentRepository
  private lateinit var seeder: Seeder

  @BeforeEach
  fun setUp() = runTest {
    featureGroupRepository = mock(FeatureGroupRepository::class.java)
    featureService = mock(FeatureService::class.java)
    environment = mock(Environment::class.java)
    propertyService = mock(PropertyService::class.java)
    productRepository = mock(ProductRepository::class.java)
    pricedProductRepository = mock(PricedProductRepository::class.java)
    userSecretRepository = mock(UserSecretRepository::class.java)
    groupRepository = mock(GroupRepository::class.java)
    userGroupAssignmentRepository = mock(UserGroupAssignmentRepository::class.java)
    userRepository = mock(UserRepository::class.java)

    seeder = Seeder(
      featureGroupRepository,
      featureService,
      environment,
      propertyService,
      productRepository,
      pricedProductRepository,
      userSecretRepository,
      userRepository,
      groupRepository,
      userGroupAssignmentRepository
    )

    `when`(propertyService.rootEmail).thenReturn("admin@foo")
    `when`(propertyService.anonymousEmail).thenReturn("anon@foo")
    `when`(propertyService.rootSecretKey).thenReturn("aSecretSecret")
    `when`(userRepository.save(any2())).thenAnswer { it.arguments[0] }
    `when`(userRepository.existsByEmail(any2())).thenReturn(false)
    `when`(featureGroupRepository.save(any2())).thenAnswer { it.arguments[0] }


    `when`(groupRepository.save(any2())).thenAnswer { it.arguments[0] }
    `when`(userSecretRepository.save(any2())).thenAnswer { it.arguments[0] }
  }

  @Test
  fun `given root user does not exist, will seed one with key`() = runTest {
    `when`(userSecretRepository.existsByValueAndOwnerId(any2(), any2())).thenReturn(false)

    seeder.onInit()

    verify(userRepository, times(1)).save(argThat { it.admin })
  }

  @Test
  fun `given root user exists, won't do anything`() = runTest {
    val root = mock(User::class.java)
    `when`(root.id).thenReturn(UserId())
    `when`(root.email).thenReturn("admin@foo")
    `when`(userRepository.findFirstByAdminIsTrue()).thenReturn(root)
    `when`(userRepository.findByEmail(eq("anon@foo"))).thenReturn(mock(User::class.java))
    `when`(userSecretRepository.existsByValueAndOwnerId(any2(), any2())).thenReturn(true)

    seeder.onInit()

    verify(userRepository, times(0)).save(argThat { !it.admin })
  }

  @Test
  fun `given admin group does not exist, will seed one`() = runTest {
    `when`(userSecretRepository.existsByValueAndOwnerId(any2(), any2())).thenReturn(true)

    seeder.onInit()

    verify(groupRepository, times(1)).save(any2())
//    verify(userGroupAssignmentRepository, times(1)).save(any2())
  }

  @Test
  @Disabled("Not implemented yet")
  fun `given admin group exists, won't do anything`() = runTest {
    val adminGroup = mock(Group::class.java)
    `when`(adminGroup.id).thenReturn(GroupId())
    `when`(groupRepository.findByName(any2())).thenReturn(adminGroup)
    `when`(userSecretRepository.existsByValueAndOwnerId(any2(), any2())).thenReturn(true)

    seeder.onInit()

    verify(groupRepository, times(0)).save(any2())
//    verify(userGroupAssignmentRepository, times(0)).save(any2())
  }
}
