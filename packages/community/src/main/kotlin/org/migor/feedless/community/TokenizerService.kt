package org.migor.feedless.community

import jakarta.annotation.PostConstruct
import opennlp.tools.tokenize.TokenizerME
import opennlp.tools.tokenize.TokenizerModel
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.util.ResourceUtils
import java.util.*


@Service
@Profile("${AppProfiles.community} & ${AppLayer.service}")
class TokenizerService {

  private lateinit var modelEn: TokenizerModel
  private lateinit var modelDe: TokenizerModel

  @PostConstruct
  fun postConstruct() {
    modelEn = loadModelFromFile("opennlp-en-ud-ewt-tokens-1.0-1.9.3.bin")
    modelDe = loadModelFromFile("opennlp-de-ud-gsd-tokens-1.0-1.9.3.bin")
  }

  private fun loadModelFromFile(filename: String) = TokenizerModel(ResourceUtils.getFile("classpath:models/$filename"))

  private fun resolveModel(locale: Locale): TokenizerModel {
    return when (locale.language) {
      "eng", "en" -> modelEn
      "deu", "de" -> modelDe
      else -> throw IllegalArgumentException("Unknown language ${locale.language}")
    }
  }

  fun tokenizeWords(value: String, locale: Locale): Array<out String> {
    val special = Regex("[^A-Za-z0-9]+")
    return TokenizerME(resolveModel(locale))
      .tokenize(value)
      .filter { !special.matches(it) }
      .toTypedArray()
  }

  fun tokenizeSentences(text: String): List<String> {
    return text.split(".!?;").filter { it.trim().length > 2 }
  }

}
