package org.migor.feedless.community.text.simple

import org.languagetool.JLanguageTool
import org.languagetool.Languages
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.community.LanguageService
import org.migor.feedless.community.TokenizerService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.util.*

@Service
@Profile("${AppProfiles.community} & ${AppLayer.service}")
class SpellingScorer(
  private val tokenizerService: TokenizerService,
  private val languageService: LanguageService
) {

  private val log = LoggerFactory.getLogger(SpellingScorer::class.simpleName)

  fun calculateErrorRate(text: String, locale: Locale): Double {
    val words = tokenizerService.tokenizeWords(text, locale)

    // https://dev.languagetool.org/java-api.html
    val langTool = JLanguageTool(Languages.getLanguageForShortCode(convertToLangCode(locale)))
    val ignoredTypes = arrayOf("Other", "Hint")
    val matches = langTool.check(text)
      .filter { !ignoredTypes.contains(it.type.name) }
      .map { Pair(it.type, text.substring(it.fromPos, it.toPos)) }

    return matches.size.toDouble() / words.size.toDouble()
  }

  private fun convertToLangCode(locale: Locale): String {
    return when (locale.language) {
      "eng", "en" -> "en-US"
      "deu", "de" -> "de-DE"
      else -> throw IllegalArgumentException("Unknown language ${locale.language}")
    }
  }

}
