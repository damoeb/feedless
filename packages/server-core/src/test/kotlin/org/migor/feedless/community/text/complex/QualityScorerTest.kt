package org.migor.feedless.community.text.complex

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.migor.feedless.community.text.simple.withUrls

class QualityScorerTest {

  private lateinit var scorer: OriginalityScorer

  @BeforeEach
  fun setUp() {
    scorer = OriginalityScorer()
  }

  @ParameterizedTest
  @CsvSource(value = [
    "0, 0.0",
    "1, 0.0",
    "2, 0.0",
    "3, 1.0",
  ])
  fun scoreHyperLinksSpamming(input: Int, expected: Double) {
    assertThat(scorer.scoreHyperLinksSpamming(withUrls(input))).isEqualTo(expected)
  }
}
