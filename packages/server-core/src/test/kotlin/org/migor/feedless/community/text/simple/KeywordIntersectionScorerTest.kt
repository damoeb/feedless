package org.migor.feedless.community.text.simple

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.migor.feedless.community.CommentEntity
import org.migor.feedless.community.LanguageService
import org.migor.feedless.community.PartOfSpeechService
import org.migor.feedless.community.StemmerService
import org.migor.feedless.community.TokenizerService
import org.mockito.Mockito
import org.mockito.Mockito.mock

class KeywordIntersectionScorerTest {

  private lateinit var scorer: KeywordIntersectionScorer
  private val parentText =
    """The American bison (Bison bison; pl.: bison), also called the American buffalo or simply buffalo (not to be confused with true buffalo), is a species of bison native to North America. It is one of two extant species of bison, alongside the European bison. Its historical range, by 9000 BCE, is described as the great bison belt, a tract of rich grassland that ran from Alaska to the Gulf of Mexico, east to the Atlantic Seaboard (nearly to the Atlantic tidewater in some areas), as far north as New York, south to Georgia, and according to some sources, further south to Florida, with sightings in North Carolina near Buffalo Ford on the Catawba River as late as 1750."""

  @BeforeEach
  fun setUp() {
    val languageService = LanguageService()
    languageService.postConstruct()

    val tokenizerService = TokenizerService()
    tokenizerService.postConstruct()

    val partOfSpeechService = PartOfSpeechService()
    partOfSpeechService.tokenizerService = tokenizerService
    partOfSpeechService.postConstruct()

    scorer = KeywordIntersectionScorer()
    scorer.languageService = languageService
    scorer.stemmerService = StemmerService()
    scorer.partOfSpeechService = partOfSpeechService
  }

  @ParameterizedTest
  @CsvSource(
    value = [
      "0.32;; 0.86;; Two subspecies or ecotypes have been described: the plains bison (B. b. bison), smaller in size and with a more rounded hump, and the wood bison (B. b. athabascae)—the larger of the two and having a taller, square hump.[9][10][11][12][13][14] Furthermore, the plains bison has been suggested to consist of a northern plains (B. b. montanae) and a southern plains (B. b. bison) subspecies, bringing the total to three.[12] However, this is generally not supported. The wood bison is one of the largest wild species of extant bovid in the world, surpassed only by the Asian gaur.[15] Among extant land animals in North America, the bison is the heaviest and the longest, and the second tallest after the moose.",
      "0.0;; 0.0;; Old English is one of the West Germanic languages, and its closest relatives are Old Frisian and Old Saxon. Like other old Germanic languages, it is very different from Modern English and Modern Scots, and largely incomprehensible for Modern English or Modern Scots speakers without study.[3] Within Old English grammar nouns, adjectives, pronouns and verbs have many inflectional endings and forms, and word order is much freer.[2] The oldest Old English inscriptions were written using a runic system, but from about the 8th century this was replaced by a version of the Latin alphabet.",
    ], delimiterString = ";; "
  )
  fun `given parent and child share keywords`(expectedIntersection: Double, expectedScore: Double, childText: String) {
    val parent = mock(CommentEntity::class.java)
    Mockito.`when`(parent.contentText).thenReturn(parentText)

    val child = mock(CommentEntity::class.java)
    Mockito.`when`(child.contentText).thenReturn(childText)

    assertThat(scorer.calculateKeywordIntersection(parent, child)).isCloseTo(expectedIntersection, within(0.01))
    assertThat(scorer.score(parent, child)).isCloseTo(expectedScore, within(0.01))
  }
}
