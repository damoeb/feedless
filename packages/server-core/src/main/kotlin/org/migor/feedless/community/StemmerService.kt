package org.migor.feedless.community

import org.migor.feedless.AppProfiles
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.tartarus.snowball.SnowballStemmer
import org.tartarus.snowball.ext.EnglishStemmer
import org.tartarus.snowball.ext.GermanStemmer
import java.util.*


@Service
@Profile(AppProfiles.community)
class StemmerService {

  fun stem(words: List<String>, locale: Locale): List<String> {
    val stemmer = tryResolveStemmer(locale)!!
    return words.map { word -> stemmer.stemWord(word) }
  }

  fun supportsLocale(locale: Locale): Boolean {
    return tryResolveStemmer(locale) != null
  }

  private fun tryResolveStemmer(locale: Locale): SnowballStemmer? {
    return when (locale.language) {
      "eng" -> EnglishStemmer()
      "deu" -> GermanStemmer()
      else -> null
    }
  }
}

private fun SnowballStemmer.stemWord(raw: String): String {
  current = raw
  stem()
  return current
}
