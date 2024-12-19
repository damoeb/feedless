package org.migor.feedless.session

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.common.PropertyService
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.security.oauth2.jwt.Jwt
import java.time.LocalDateTime
import java.time.ZoneOffset

class CookieProviderTest {

  private lateinit var cookieProvider: CookieProvider

  @BeforeEach
  fun setUp() {
    val propertyService = mock(PropertyService::class.java)
    `when`(propertyService.domain).thenReturn("foo.bar")
    cookieProvider = CookieProvider(propertyService)
  }

  @Test
  fun createTokenCookie() = runTest {
    // given
    val jwt = mock(Jwt::class.java)
    `when`(jwt.tokenValue).thenReturn("token")
    `when`(jwt.expiresAt).thenReturn(LocalDateTime.now().plusSeconds(123).toInstant(ZoneOffset.UTC))

    // when
    val cookie = cookieProvider.createTokenCookie(jwt)

    // then
    assertThat(cookie.isHttpOnly).isTrue()
    assertThat(cookie.maxAge).isGreaterThan(123 - 2)
  }
}
