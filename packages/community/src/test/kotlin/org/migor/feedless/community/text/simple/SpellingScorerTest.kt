package org.migor.feedless.community.text.simple

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.migor.feedless.community.LanguageService
import org.migor.feedless.community.TokenizerService
import java.util.*

@Tag("nlp")
class SpellingScorerTest {

  private lateinit var scorer: SpellingScorer

  @BeforeEach
  fun setUp() {

    val languageService = LanguageService()
    languageService.postConstruct()

    val tokenizerService = TokenizerService()
    tokenizerService.postConstruct()

    scorer = SpellingScorer(tokenizerService, languageService)
  }

  @ParameterizedTest
  @CsvSource(
    value = [
      "0.03;; en;; Two subspecies or ecotypes have been described: the plains bison (B. b. bison), smaller in size and with a more rounded hump, and the wood bison (B. b. athabascae)—the larger of the two and having a taller, square hump.[9][10][11][12][13][14] Furthermore, the plains bison has been suggested to consist of a northern plains (B. b. montanae) and a southern plains (B. b. bison) subspecies, bringing the total to three.[12] However, this is generally not supported. The wood bison is one of the largest wild species of extant bovid in the world, surpassed only by the Asian gaur.[15] Among extant land animals in North America, the bison is the heaviest and the longest, and the second tallest after the moose.",
//      "0.11;; de;; Eine Milliarde Euro will die EU dem Libanon zahlen, damite das Lande im Gegenzug die irreguläre Migration syrischer Kriegsflüchtlinge stoppt. Legale Wöge sollen jedoc ofen bleiben.",
//      "0.0;; en;; Old English is one of the West Germanic languages, and its closest relatives are Old Frisian and Old Saxon. Like other old Germanic languages, it is very different from Modern English and Modern Scots, and largely incomprehensible for Modern English or Modern Scots speakers without study.[3] Within Old English grammar nouns, adjectives, pronouns and verbs have many inflectional endings and forms, and word order is much freer.[2] The oldest Old English inscriptions were written using a runic system, but from about the 8th century this was replaced by a version of the Latin alphabet.",
    ], delimiterString = ";; "
  )
  fun spelling(expected: Double, inputLang: String, inputText: String) {
    assertThat(scorer.calculateErrorRate(inputText, Locale.of(inputLang))).isCloseTo(expected, within(0.01))
  }

}
