package org.migor.feedless.community.text.simple

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.community.TokenizerService
import org.migor.feedless.community.text.hyphenation.HyphenationPattern
import org.migor.feedless.community.text.hyphenation.Hyphenator
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.util.*

@Service
@Profile("${AppProfiles.community} & ${AppLayer.service}")
class ReadingEaseScorer(
  private val tokenizerService: TokenizerService
) {

  private val log = LoggerFactory.getLogger(ReadingEaseScorer::class.simpleName)

  private final val FALLBACK_READING_EASE_SCORE = 0.3

  fun score(text: String, locale: Locale): Double {
    return try {
      HyphenationPattern.lookup(locale.language)?.let { hyphenationPattern ->
        Hyphenator.getInstance(hyphenationPattern)?.let { hyphenator ->
          val words = tokenizerService.tokenizeWords(text, locale)
          val totalWords = words.size.toDouble()
          val totalSentences = tokenizerService.tokenizeSentences(text).size
          val totalSyllables = words.sumOf { hyphenator.hyphenate(it).size }

          // flesh reading ease https://readable.com/readability/flesch-reading-ease-flesch-kincaid-grade-level/
          206.835 - 1.015 * (totalWords / totalSentences) - 84.6 * (totalSyllables / totalWords)
        }
      } ?: FALLBACK_READING_EASE_SCORE

    } catch (e: IllegalArgumentException) {
      log.warn(e.message)
      FALLBACK_READING_EASE_SCORE
    }
  }
}
