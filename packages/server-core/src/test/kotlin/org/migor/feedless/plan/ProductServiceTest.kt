package org.migor.feedless.plan

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.migor.feedless.Mother.randomUserId
import org.migor.feedless.Vertical
import org.migor.feedless.any2
import org.migor.feedless.data.jpa.plan.PlanDAO
import org.migor.feedless.data.jpa.pricedProduct.PricedProductDAO
import org.migor.feedless.data.jpa.product.ProductDAO
import org.migor.feedless.data.jpa.product.ProductEntity
import org.migor.feedless.data.jpa.user.UserDAO
import org.migor.feedless.data.jpa.user.UserEntity
import org.migor.feedless.user.UserId
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.util.*

class ProductServiceTest {

  private lateinit var service: ProductService
  private lateinit var productDAO: ProductDAO
  private lateinit var userDAO: UserDAO
  private lateinit var planDAO: PlanDAO
  private lateinit var pricedProductDAO: PricedProductDAO
  private lateinit var userId: UserId

  @BeforeEach
  fun setUp() {
    userId = randomUserId()
    productDAO = mock(ProductDAO::class.java)
    userDAO = mock(UserDAO::class.java)
    planDAO = mock(PlanDAO::class.java)
    pricedProductDAO = mock(PricedProductDAO::class.java)
    service = ProductService(
      productDAO,
      userDAO,
      planDAO,
      pricedProductDAO,
    )
  }

  @Test
  @Disabled
  fun `enableDefaultSaasProduct`() = runTest {
    `when`(productDAO.findByPartOfAndBaseProductIsTrue(any2())).thenReturn(mock(ProductEntity::class.java))
    `when`(userDAO.findById(any2())).thenReturn(Optional.of(mock(UserEntity::class.java)))

    service.enableDefaultSaasProduct(Vertical.feedless, userId)

    verify(planDAO).save(any2())
  }
}
