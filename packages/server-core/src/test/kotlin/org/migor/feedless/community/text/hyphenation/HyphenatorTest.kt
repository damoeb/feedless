package org.migor.feedless.community.text.hyphenation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class HyphenatorTest {

  @ParameterizedTest
  @CsvSource(
    value = [
      "Kochschule, Koch-schu-le",
      "Seewetterdienst, See-wet-ter-dienst",
      "Hochverrat, Hoch-ver-rat",
      "Musterbeispiel, Mus-ter-bei-spiel",
      "Bundespräsident, Bun-des-prä-si-dent",
      "Schmetterling, Schmet-ter-ling",
      "Christian, Chris-ti-an"
    ]
  )
  fun testDe(input: String, expected: String) {
    val de = HyphenationPattern.lookup("de")
    val h = Hyphenator.getInstance(de!!)
    val actual: String = h!!.hyphenate(input).joinToString("-")
    assertEquals(expected, actual)
  }

  @ParameterizedTest
  @CsvSource(
    value = [
      "crocodile, croc-o-dile",
      "activity, ac-tiv-ity",
//    "potato, po-ta-to",
      "hyphenation, hy-phen-ation",
      "podcast, pod-cast",
      "message, mes-sage"
    ]
  )
  fun testEnUs(input: String, expected: String) {
    val us = HyphenationPattern.lookup("en_us")
    val h = Hyphenator.getInstance(us!!)
    val actual: String = h!!.hyphenate(input).joinToString("-")
    assertEquals(expected, actual)
  }
}
