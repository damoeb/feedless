package org.migor.feedless.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CryptUtilTest {

  @Test
  fun `new share keys keep the length and alphabet of existing keys`() {
    val keys = (1..200).map { CryptUtil.newShareKey() }

    assertThat(keys).allMatch { it.matches(Regex("[A-Za-z0-9]{9}")) }
    assertThat(keys.toSet()).hasSize(200)
  }
}
