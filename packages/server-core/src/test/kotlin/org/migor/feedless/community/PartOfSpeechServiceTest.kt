package org.migor.feedless.community

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.util.*

@Tag("nlp")
class PartOfSpeechServiceTest {

  private lateinit var service: PartOfSpeechService

  @BeforeEach
  fun setUp() {
    val tokenizerService = TokenizerService()
    tokenizerService.postConstruct()

    service = PartOfSpeechService()
    service.tokenizerService = tokenizerService
    service.postConstruct()
  }

  @Test
  fun tag() {
    val tagged = service.tag("John is 27 years old.", Locale.ENGLISH)
    assertThat(tagged)
      .isEqualTo(listOf(Pair("John", "PROPN"), Pair("is", "AUX"), Pair("27", "NUM"), Pair("years", "NOUN"), Pair("old", "ADJ")))
  }
}
