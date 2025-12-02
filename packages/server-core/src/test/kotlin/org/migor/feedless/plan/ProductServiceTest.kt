package org.migor.feedless.plan

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.migor.feedless.Mother.randomUserId
import org.migor.feedless.Vertical
import org.migor.feedless.any2
import org.migor.feedless.pricedProduct.PricedProductRepository
import org.migor.feedless.product.Product
import org.migor.feedless.product.ProductRepository
import org.migor.feedless.product.ProductService
import org.migor.feedless.product.ProductServiceImpl
import org.migor.feedless.user.User
import org.migor.feedless.user.UserId
import org.migor.feedless.user.UserRepository
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class ProductServiceTest {

  private lateinit var service: ProductService
  private lateinit var productRepository: ProductRepository
  private lateinit var userRepository: UserRepository
  private lateinit var planRepository: PlanRepository
  private lateinit var pricedProductRepository: PricedProductRepository
  private lateinit var userId: UserId

  @BeforeEach
  fun setUp() {
    userId = randomUserId()
    productRepository = mock(ProductRepository::class.java)
    userRepository = mock(UserRepository::class.java)
    planRepository = mock(PlanRepository::class.java)
    pricedProductRepository = mock(PricedProductRepository::class.java)
    service = ProductServiceImpl(
      productRepository,
      userRepository,
      planRepository,
      pricedProductRepository,
    )
  }

  @Test
  @Disabled
  fun `enableDefaultSaasProduct`() = runTest {
    `when`(productRepository.findByPartOfAndBaseProductIsTrue(any2())).thenReturn(mock(Product::class.java))
    `when`(userRepository.findById(any2())).thenReturn(mock(User::class.java))

    service.enableDefaultSaasProduct(Vertical.feedless, userId)

    verify(planRepository).save(any2())
  }
}
