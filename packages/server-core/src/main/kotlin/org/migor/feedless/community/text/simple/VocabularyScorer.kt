package org.migor.feedless.community.text.simple

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.community.StemmerService
import org.migor.feedless.community.TokenizerService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.community} & ${AppLayer.service}")
class VocabularyScorer(
  private val stemmerService: StemmerService,
  private val tokenizerService: TokenizerService
) {

  private val log = LoggerFactory.getLogger(VocabularyScorer::class.simpleName)

  fun score(text: String, locale: Locale): Double {
    return if (text.isEmpty()) {
      0.0
    } else {
      if (stemmerService.supportsLocale(locale)) {
        calculateTypeTokenRatio(text, locale)
      } else {
        0.2
      }
    }
  }

  /**
   * MTLD (measure of textual lexical diversity)
   * https://www.reuneker.nl/files/ld/
   * http://de.wikipedia.org/wiki/Type-Token-Relation
   */
  private fun calculateTypeTokenRatio(text: String, locale: Locale): Double {
    val words = tokenizerService.tokenizeWords(text, locale).map { it.lowercase() }
    val stemmedWords = stemmerService.stem(words, locale)
    val types = stemmedWords.toSet().size.toDouble()
    val tokens = stemmedWords.size.toDouble()

    return if (tokens == 0.0) {
      0.0
    } else {
      types / tokens
    }
  }
}
