package org.migor.feedless.config

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.migor.feedless.capability.SecurityContextCapabilityService
import org.migor.feedless.session.LazyGrantedAuthority
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder

class CustomSecurityExpressionRootTest {

  @Test
  fun `hasCapability returns true when capability exists`() = runTest {
    // given
    val authentication = mock(Authentication::class.java)
    val authorities = listOf(
      LazyGrantedAuthority("user", """{"id":"test-user-id"}"""),
      LazyGrantedAuthority("groups", """[]""")
    )
    `when`(authentication.authorities).thenReturn(authorities)

    mockAuthentication(authentication) {
      val expressionRoot = SecurityContextCapabilityService()

      // when & then
      assertThat(expressionRoot.hasCapability("user")).isTrue()
      assertThat(expressionRoot.hasCapability("groups")).isTrue()
    }
  }

  @Test
  fun `hasCapability returns false when capability does not exist`() = runTest {
    // given
    val authentication = mock(Authentication::class.java)
    val authorities = listOf(
      LazyGrantedAuthority("user", """{"id":"test-user-id"}""")
    )
    `when`(authentication.authorities).thenReturn(authorities)

    mockAuthentication(authentication) {
      val expressionRoot = SecurityContextCapabilityService()

      // when & then
      assertThat(expressionRoot.hasCapability("agent")).isFalse()
      assertThat(expressionRoot.hasCapability("admin")).isFalse()
      assertThat(expressionRoot.hasCapability("nonexistent")).isFalse()
    }
  }

  @Test
  fun `hasCapability returns false when authorities list is empty`() = runTest {
    // given
    val authentication = mock(Authentication::class.java)
    `when`(authentication.authorities).thenReturn(emptyList())

    mockAuthentication(authentication) {
      val expressionRoot = SecurityContextCapabilityService()

      // when & then
      assertThat(expressionRoot.hasCapability("user")).isFalse()
      assertThat(expressionRoot.hasCapability("agent")).isFalse()
    }
  }

  @Test
  fun `hasCapability works with multiple capabilities`() = runTest {
    // given
    val authentication = mock(Authentication::class.java)
    val authorities = listOf(
      LazyGrantedAuthority("user", """{"id":"test-user-id"}"""),
      LazyGrantedAuthority("agent", """{"dummy":"value"}"""),
      LazyGrantedAuthority("groups", """[]""")
    )
    `when`(authentication.authorities).thenReturn(authorities)
    mockAuthentication(authentication) {
      val expressionRoot = SecurityContextCapabilityService()

      // when & then
      assertThat(expressionRoot.hasCapability("user")).isTrue()
      assertThat(expressionRoot.hasCapability("agent")).isTrue()
      assertThat(expressionRoot.hasCapability("groups")).isTrue()
      assertThat(expressionRoot.hasCapability("admin")).isFalse()
    }
  }

  @Test
  fun `hasToken returns true when authentication is authenticated`() = runTest {
    // given
    val authentication = mock(Authentication::class.java)
    `when`(authentication.isAuthenticated).thenReturn(true)

    mockAuthentication(authentication) {
      val expressionRoot = SecurityContextCapabilityService()

      // when & then
      assertThat(expressionRoot.hasToken()).isTrue()
    }
  }

  @Test
  fun `hasToken returns false when authentication is not authenticated`() = runTest {
    // given
    val authentication = mock(Authentication::class.java)
    `when`(authentication.isAuthenticated).thenReturn(false)
    mockAuthentication(authentication) {
      val expressionRoot = SecurityContextCapabilityService()

      // when & then
      assertThat(expressionRoot.hasToken()).isFalse()
    }
  }

  private fun mockAuthentication(authentication: Authentication, block: () -> Any) {
    Mockito.mockStatic(SecurityContextHolder::class.java).use { staticMock ->
      {
        val securityContext = mock(SecurityContext::class.java)
        `when`(securityContext.authentication).thenReturn(authentication)
        `when`(SecurityContextHolder.getContext()).thenReturn(securityContext)

        block.invoke()
      }
    }
  }
}

