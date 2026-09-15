package org.migor.feedless.api.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.migor.feedless.user.UserId
import org.migor.feedless.userSecret.UserSecret
import org.migor.feedless.userSecret.UserSecretType
import org.springframework.test.util.ReflectionTestUtils
import java.time.LocalDateTime

class UserSecretMapperTest {

  private val mapper = UserSecretMapperImpl().also {
    ReflectionTestUtils.setField(it, "enumMapper", EnumMapperImpl())
  }

  @Test
  fun `masks a token down to its distinct end`() {
    val dto = mapper.toDto(secret("eyJhbGciOiJIUzI1NiJ9.payload.signatureA1b2C3"), true)

    assertThat(dto.value).isEqualTo("••••A1b2C3")
    assertThat(dto.valueMasked).isTrue()
  }

  @Test
  fun `two tokens with the same prefix mask differently`() {
    val first = mapper.toDto(secret("eyJhbGciOiJIUzI1NiJ9.payload.sigAAAAAA"), true)
    val second = mapper.toDto(secret("eyJhbGciOiJIUzI1NiJ9.payload.sigBBBBBB"), true)

    assertThat(first.value).isNotEqualTo(second.value)
  }

  @Test
  fun `never reveals a short value while masking`() {
    assertThat(mapper.toDto(secret("short"), true).value).isEqualTo("••••")
  }

  @Test
  fun `shows the whole value unmasked`() {
    val dto = mapper.toDto(secret("eyJhbGciOiJIUzI1NiJ9.payload.signature"), false)

    assertThat(dto.value).isEqualTo("eyJhbGciOiJIUzI1NiJ9.payload.signature")
    assertThat(dto.valueMasked).isFalse()
  }

  @Test
  fun `passes the name through`() {
    assertThat(mapper.toDto(secret("eyJhbGciOiJIUzI1NiJ9.payload.signature"), true).name).isEqualTo("laptop")
  }

  private fun secret(value: String) = UserSecret(
    name = "laptop",
    value = value,
    validUntil = LocalDateTime.now().plusDays(1),
    type = UserSecretType.SecretKey,
    ownerId = UserId(),
  )
}
