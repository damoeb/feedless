package org.migor.feedless.community.text.simple

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.migor.feedless.community.StemmerService
import org.migor.feedless.community.TokenizerService
import java.util.*

@Tag("nlp")
class VocabularyScorerTest {

  private lateinit var scorer: VocabularyScorer

  @BeforeEach
  fun setUp() {
    val tokenizerService = TokenizerService()
    tokenizerService.postConstruct()

    scorer = VocabularyScorer()
    scorer.stemmerService = StemmerService()
    scorer.tokenizerService = tokenizerService
  }

  @Test
  fun score_english() {
    val comment =
      "Two subspecies or ecotypes have been described: the plains bison (B. b. bison), smaller in size and with a more rounded hump, and the wood bison (B. b. athabascae)—the larger of the two and having a taller, square hump."
    assertThat(scorer.score(comment, Locale.ENGLISH)).isCloseTo(0.2, within(0.01))
  }
}
