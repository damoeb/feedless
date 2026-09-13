package org.migor.feedless.capability

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.session.LazyGrantedAuthority
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.user.DefaultOAuth2User

class SecurityContextCapabilityServiceTest {

  private val capabilityService = SecurityContextCapabilityService()
  private lateinit var previousContext: SecurityContext

  @BeforeEach
  fun saveContext() {
    previousContext = SecurityContextHolder.getContext()
  }

  @AfterEach
  fun restoreContext() {
    SecurityContextHolder.setContext(previousContext)
  }

  @Test
  fun `a request without authentication has no capabilities`() {
    SecurityContextHolder.clearContext()

    assertThat(capabilityService.hasCapability(UserCapability.ID)).isFalse()
    assertThat(capabilityService.getCapability(UserCapability.ID)).isNull()
    assertThat(capabilityService.hasToken()).isFalse()
  }

  @Test
  fun `an authentication with the capability provides it`() {
    val authorities = listOf(LazyGrantedAuthority(UserCapability.ID.value, "payload"))
    val principal = DefaultOAuth2User(authorities, mapOf("id" to "test"), "id")
    SecurityContextHolder.setContext(SecurityContextImpl(OAuth2AuthenticationToken(principal, authorities, "test")))

    assertThat(capabilityService.hasCapability(UserCapability.ID)).isTrue()
    assertThat(capabilityService.getCapability(UserCapability.ID)?.capabilityPayload).isEqualTo("payload")
    assertThat(capabilityService.hasToken()).isTrue()
    assertThat(capabilityService.hasCapability(CapabilityId("agent"))).isFalse()
  }
}
