package org.migor.feedless.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.migor.feedless.AppProfiles
import org.migor.feedless.session.JwtRequestFilter
import org.mockito.Mockito.mock
import org.springframework.context.annotation.Profile
import org.springframework.test.util.ReflectionTestUtils

/**
 * JwtRequestFilter is a @Component, so Spring Boot auto-registers it as a container filter on top of the
 * copy SecurityConfig adds to the security chain under oauth; without this, every request authenticates
 * its JWT twice. conditionalOauth only adds the filter to the chain under oauth, so disabling the
 * container registration outside that profile would leave the filter running nowhere at all.
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

  @Test
  fun `the registration bean is scoped to the oauth profile, where the chain also runs the filter`() {
    val method = SecurityConfig::class.java.getDeclaredMethod("jwtRequestFilterRegistration")

    val profile = method.getAnnotation(Profile::class.java)

    assertThat(profile).isNotNull()
    assertThat(profile.value).containsExactly(AppProfiles.oauth)
  }
}
