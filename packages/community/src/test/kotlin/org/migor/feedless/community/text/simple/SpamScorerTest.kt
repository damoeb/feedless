package org.migor.feedless.community.text.simple

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

@Tag("nlp")
class SpamScorerTest {

  private lateinit var scorer: SpamScorer

  @BeforeEach
  fun setUp() {
    scorer = SpamScorer()
  }

  @ParameterizedTest
  @CsvSource(
    value = [
      "0, 0.0",
      "1, 0.0",
      "2, 0.0",
      "3, 1.0"
    ]
  )
  fun scoreHyperlinks(input: Int, expected: Double) {
    assertThat(scorer.score(withUrls(input))).isEqualTo(expected)
  }
}
