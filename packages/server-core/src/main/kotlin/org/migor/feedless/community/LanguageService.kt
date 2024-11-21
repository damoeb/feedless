package org.migor.feedless.community

import jakarta.annotation.PostConstruct
import opennlp.tools.langdetect.Language
import opennlp.tools.langdetect.LanguageDetector
import opennlp.tools.langdetect.LanguageDetectorME
import opennlp.tools.langdetect.LanguageDetectorModel
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.ResourceUtils
import java.util.*


@Service
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.community} & ${AppLayer.service}")
class LanguageService {

  private lateinit var languageDetector: LanguageDetector

  @PostConstruct
  fun postConstruct() {
    val model = LanguageDetectorModel(ResourceUtils.getFile("classpath:models/langdetect-183.bin"))
    languageDetector = LanguageDetectorME(model)
  }

  suspend fun bestLanguage(text: String): Language {
    return languageDetector.predictLanguage(text)
  }

  suspend fun bestLocale(text: String): Locale {
    return Locale.of(bestLanguage(text).lang)
  }

}
