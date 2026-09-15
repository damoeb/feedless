package org.migor.feedless.http

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.security.MessageDigest
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.HexFormat

class ETagCalculatorTest {

  // Declared out of alphabetical order, so a mapper that sorts properties changes the hash.
  data class Sample(val name: String, val at: OffsetDateTime)

  @Test
  fun `hashes declaration-ordered JSON with ISO dates`() {
    val value = Sample(name = "a", at = OffsetDateTime.of(2026, 9, 13, 10, 15, 30, 0, ZoneOffset.UTC))
    val json = """{"name":"a","at":"2026-09-13T10:15:30Z"}"""
    val expected = "\"" + HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(json.toByteArray())) + "\""

    assertThat(ETagCalculator().compute(value)).isEqualTo(expected)
  }
}
