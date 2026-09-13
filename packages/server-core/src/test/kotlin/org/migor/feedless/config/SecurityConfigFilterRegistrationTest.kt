package org.migor.feedless.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.migor.feedless.session.JwtRequestFilter
import org.mockito.Mockito.mock
import org.springframework.test.util.ReflectionTestUtils

/**
 * JwtRequestFilter is a @Component, so Spring Boot auto-registers it as a container filter on top of the
 * copy SecurityConfig adds to the security chain under oauth; without this, every request authenticates
 * its JWT twice.
 */
class SecurityConfigFilterRegistrationTest {

  @Test
  fun `the servlet-container registration of JwtRequestFilter is disabled`() {
    val securityConfig = SecurityConfig()
    val filter = mock(JwtRequestFilter::class.java)
    ReflectionTestUtils.setField(securityConfig, "jwtRequestFilter", filter)

    val registration = securityConfig.jwtRequestFilterRegistration()

    assertThat(registration.isEnabled).isFalse()
    assertThat(registration.filter).isSameAs(filter)
  }
}
