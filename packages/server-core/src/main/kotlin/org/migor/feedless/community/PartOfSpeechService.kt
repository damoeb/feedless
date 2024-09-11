package org.migor.feedless.community

import jakarta.annotation.PostConstruct
import opennlp.tools.postag.POSModel
import opennlp.tools.postag.POSTaggerME
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.util.ResourceUtils
import java.util.*


@Service
@Profile("${AppProfiles.community} & ${AppLayer.service}")
class PartOfSpeechService {

  private lateinit var modelEn: POSModel
  private lateinit var modelDe: POSModel

  @Autowired
  lateinit var tokenizerService: TokenizerService

  @PostConstruct
  fun postConstruct() {
    modelEn = loadModelFromFile("opennlp-en-ud-ewt-pos-1.0-1.9.3.bin")
    modelDe = loadModelFromFile("opennlp-de-ud-gsd-pos-1.0-1.9.3.bin")
  }

  private fun loadModelFromFile(filename: String) = POSModel(ResourceUtils.getFile("classpath:models/$filename"))

  private fun resolveTagger(locale: Locale): POSTaggerME {
    return when (locale.language) {
      "eng", "en" -> POSTaggerME(modelEn)
      "deu", "de" -> POSTaggerME(modelDe)
      else -> throw IllegalArgumentException("Unknown language ${locale.language}")
    }
  }

  /**
   * Penn Treebank Tags
   * https://www.ling.upenn.edu/courses/Fall_2003/ling001/penn_treebank_pos.html
   */
  fun tag(sentence: String, locale: Locale): List<Pair<String, String>> {
    val tokens = tokenizerService.tokenizeWords(sentence, locale)
    return resolveTagger(locale)
      .tag(tokens)
      .mapIndexed { index, tag -> Pair(tokens[index], tag) }
  }
}
