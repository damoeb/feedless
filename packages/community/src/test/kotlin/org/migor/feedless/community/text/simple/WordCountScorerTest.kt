package org.migor.feedless.community.text.simple

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.migor.feedless.community.TokenizerService
import java.util.*

@Tag("nlp")
class WordCountScorerTest {

  private lateinit var scorer: WordCountScorer

  @BeforeEach
  fun setUp() {
    val tokenizerService = TokenizerService()
    tokenizerService.postConstruct()

    scorer = WordCountScorer(tokenizerService)
  }

  @ParameterizedTest
  @CsvSource(
    value = [
      "1, 0.0",
      "8, 0.28",
      "53, 0.99",
      "133, 0.7",
      "500, 0.6",
      "600, 0.59",
    ]
  )
  fun scoreWordCount(input: Int, expected: Double) {
    assertThat(scorer.score(ofWords(input), Locale.ENGLISH)).isCloseTo(expected, within(0.01))
  }

  private fun ofWords(wordCount: Int): String {
    val words = mutableListOf<String>()
    for (i in 1..wordCount) {
      words.add("word")
    }
    return words.joinToString(" ")
  }
}
