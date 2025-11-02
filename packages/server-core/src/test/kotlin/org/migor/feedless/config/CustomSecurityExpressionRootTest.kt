package org.migor.feedless.config

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.migor.feedless.capability.SecurityContextCapabilityService
import org.migor.feedless.session.LazyGrantedAuthority
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority

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

    val expressionRoot = SecurityContextCapabilityService(authentication)

    // when & then
    assertThat(expressionRoot.hasCapability("user")).isTrue()
    assertThat(expressionRoot.hasCapability("groups")).isTrue()
  }

  @Test
  fun `hasCapability returns false when capability does not exist`() = runTest {
    // given
    val authentication = mock(Authentication::class.java)
    val authorities = listOf(
      LazyGrantedAuthority("user", """{"id":"test-user-id"}""")
    )
    `when`(authentication.authorities).thenReturn(authorities)

    val expressionRoot = SecurityContextCapabilityService(authentication)

    // when & then
    assertThat(expressionRoot.hasCapability("agent")).isFalse()
    assertThat(expressionRoot.hasCapability("admin")).isFalse()
    assertThat(expressionRoot.hasCapability("nonexistent")).isFalse()
  }

  @Test
  fun `hasCapability returns false when authorities list is empty`() = runTest {
    // given
    val authentication = mock(Authentication::class.java)
    `when`(authentication.authorities).thenReturn(emptyList())

    val expressionRoot = SecurityContextCapabilityService(authentication)

    // when & then
    assertThat(expressionRoot.hasCapability("user")).isFalse()
    assertThat(expressionRoot.hasCapability("agent")).isFalse()
  }

  @Test
  fun `hasCapability is case sensitive`() = runTest {
    // given
    val authentication = mock(Authentication::class.java)
    val authorities = listOf(
      LazyGrantedAuthority("user", """{"id":"test-user-id"}""")
    )
    `when`(authentication.authorities).thenReturn(authorities)

    val expressionRoot = SecurityContextCapabilityService(authentication)

    // when & then
    assertThat(expressionRoot.hasCapability("user")).isTrue()
    assertThat(expressionRoot.hasCapability("USER")).isFalse()
    assertThat(expressionRoot.hasCapability("User")).isFalse()
  }

  @Test
  fun `hasCapability ignores non-LazyGrantedAuthority authorities`() = runTest {
    // given
    val authentication = mock(Authentication::class.java)
    val authorities: List<GrantedAuthority> = listOf(
      SimpleGrantedAuthority("ROLE_USER"),
      LazyGrantedAuthority("user", """{"id":"test-user-id"}"""),
      SimpleGrantedAuthority("ROLE_ADMIN")
    )
    `when`(authentication.authorities).thenReturn(authorities)

    val expressionRoot = SecurityContextCapabilityService(authentication)

    // when & then
    assertThat(expressionRoot.hasCapability("user")).isTrue()
    assertThat(expressionRoot.hasCapability("ROLE_USER")).isFalse()
    assertThat(expressionRoot.hasCapability("ROLE_ADMIN")).isFalse()
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

    val expressionRoot = SecurityContextCapabilityService(authentication)

    // when & then
    assertThat(expressionRoot.hasCapability("user")).isTrue()
    assertThat(expressionRoot.hasCapability("agent")).isTrue()
    assertThat(expressionRoot.hasCapability("groups")).isTrue()
    assertThat(expressionRoot.hasCapability("admin")).isFalse()
  }

  @Test
  fun `hasToken returns true when authentication is authenticated`() = runTest {
    // given
    val authentication = mock(Authentication::class.java)
    `when`(authentication.isAuthenticated).thenReturn(true)

    val expressionRoot = SecurityContextCapabilityService(authentication)

    // when & then
    assertThat(expressionRoot.hasToken()).isTrue()
  }

  @Test
  fun `hasToken returns false when authentication is not authenticated`() = runTest {
    // given
    val authentication = mock(Authentication::class.java)
    `when`(authentication.isAuthenticated).thenReturn(false)

    val expressionRoot = SecurityContextCapabilityService(authentication)

    // when & then
    assertThat(expressionRoot.hasToken()).isFalse()
  }

  @Test
  fun `hasToken returns true for authenticated user with capabilities`() = runTest {
    // given
    val authentication = mock(Authentication::class.java)
    `when`(authentication.isAuthenticated).thenReturn(true)
    val authorities = listOf(
      LazyGrantedAuthority("user", """{"id":"test-user-id"}""")
    )
    `when`(authentication.authorities).thenReturn(authorities)

    val expressionRoot = SecurityContextCapabilityService(authentication)

    // when & then
    assertThat(expressionRoot.hasToken()).isTrue()
    assertThat(expressionRoot.hasCapability("user")).isTrue()
  }

  @Test
  fun `hasToken returns true even without capabilities`() = runTest {
    // given
    val authentication = mock(Authentication::class.java)
    `when`(authentication.isAuthenticated).thenReturn(true)
    `when`(authentication.authorities).thenReturn(emptyList())

    val expressionRoot = SecurityContextCapabilityService(authentication)

    // when & then
    assertThat(expressionRoot.hasToken()).isTrue()
    assertThat(expressionRoot.hasCapability("user")).isFalse()
  }

  @Test
  fun `combined test - hasToken true but specific capability false`() = runTest {
    // given
    val authentication = mock(Authentication::class.java)
    `when`(authentication.isAuthenticated).thenReturn(true)
    val authorities = listOf(
      LazyGrantedAuthority("user", """{"id":"test-user-id"}""")
    )
    `when`(authentication.authorities).thenReturn(authorities)

    val expressionRoot = SecurityContextCapabilityService(authentication)

    // when & then
    assertThat(expressionRoot.hasToken()).isTrue()
    assertThat(expressionRoot.hasCapability("user")).isTrue()
    assertThat(expressionRoot.hasCapability("agent")).isFalse()
  }

  @Test
  fun `hasCapability with empty string returns false`() = runTest {
    // given
    val authentication = mock(Authentication::class.java)
    val authorities = listOf(
      LazyGrantedAuthority("user", """{"id":"test-user-id"}""")
    )
    `when`(authentication.authorities).thenReturn(authorities)

    val expressionRoot = SecurityContextCapabilityService(authentication)

    // when & then
    assertThat(expressionRoot.hasCapability("")).isFalse()
  }

  @Test
  fun `hasCapability with special characters in capability ID`() = runTest {
    // given
    val authentication = mock(Authentication::class.java)
    val authorities = listOf(
      LazyGrantedAuthority("special-capability-123", """{"id":"test"}""")
    )
    `when`(authentication.authorities).thenReturn(authorities)

    val expressionRoot = SecurityContextCapabilityService(authentication)

    // when & then
    assertThat(expressionRoot.hasCapability("special-capability-123")).isTrue()
    assertThat(expressionRoot.hasCapability("special-capability-456")).isFalse()
  }

  @Test
  fun `real world scenario - user capability check`() = runTest {
    // given - simulating a JWT token with user capability
    val authentication = mock(Authentication::class.java)
    `when`(authentication.isAuthenticated).thenReturn(true)
    val authorities = listOf(
      LazyGrantedAuthority("user", """{"id":"550e8400-e29b-41d4-a716-446655440000"}"""),
      LazyGrantedAuthority("groups", """[]""")
    )
    `when`(authentication.authorities).thenReturn(authorities)

    val expressionRoot = SecurityContextCapabilityService(authentication)

    // when & then - user can access user-protected endpoints
    assertThat(expressionRoot.hasToken()).isTrue()
    assertThat(expressionRoot.hasCapability("user")).isTrue()
    assertThat(expressionRoot.hasCapability("agent")).isFalse()
  }

  @Test
  fun `real world scenario - agent capability check`() = runTest {
    // given - simulating an agent/service token
    val authentication = mock(Authentication::class.java)
    `when`(authentication.isAuthenticated).thenReturn(true)
    val authorities = listOf(
      LazyGrantedAuthority("agent", """{"dummy":"service-123"}""")
    )
    `when`(authentication.authorities).thenReturn(authorities)

    val expressionRoot = SecurityContextCapabilityService(authentication)

    // when & then - agent can only access agent-protected endpoints
    assertThat(expressionRoot.hasToken()).isTrue()
    assertThat(expressionRoot.hasCapability("agent")).isTrue()
    assertThat(expressionRoot.hasCapability("user")).isFalse()
  }

  @Test
  fun `real world scenario - anonymous token-only access`() = runTest {
    // given - simulating a token without specific capabilities (anonymous)
    val authentication = mock(Authentication::class.java)
    `when`(authentication.isAuthenticated).thenReturn(true)
    `when`(authentication.authorities).thenReturn(emptyList())

    val expressionRoot = SecurityContextCapabilityService(authentication)

    // when & then - has token but no specific capabilities
    assertThat(expressionRoot.hasToken()).isTrue()
    assertThat(expressionRoot.hasCapability("user")).isFalse()
    assertThat(expressionRoot.hasCapability("agent")).isFalse()
  }
}

