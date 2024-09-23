package org.migor.feedless.community.text.simple

import org.apache.commons.math3.analysis.interpolation.SplineInterpolator
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.community.CommentEntity
import org.migor.feedless.community.LanguageService
import org.migor.feedless.community.PartOfSpeechService
import org.migor.feedless.community.StemmerService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.util.*

@Service
@Profile("${AppProfiles.community} & ${AppLayer.service}")
class KeywordIntersectionScorer(
  private val partOfSpeechService: PartOfSpeechService,
  private val languageService: LanguageService,
  private val stemmerService: StemmerService
) {

  private val log = LoggerFactory.getLogger(KeywordIntersectionScorer::class.simpleName)

  private var spline: PolynomialSplineFunction = createSplineInterpolator()

  suspend fun score(parent: CommentEntity, child: CommentEntity): Double {
    return spline.value(calculateKeywordIntersection(parent, child))
  }

  suspend fun calculateKeywordIntersection(parent: CommentEntity, child: CommentEntity): Double {
    val parentLocale = languageService.bestLocale(parent.text)
    val childLocale = languageService.bestLocale(child.text)

    return if (parentLocale.language != childLocale.language) {
      0.1
    } else {
      val parentKeywords = getKeywordsWIthFreq(parent, parentLocale)
      val childKeywords = getKeywordsWIthFreq(child, parentLocale)
      parentKeywords.filter { (word, _) -> childKeywords.any { (otherWord, _) -> otherWord == word } }
        .map { (_, freq) -> freq }
        .sum()
    }
  }

  private fun createSplineInterpolator(): PolynomialSplineFunction {
    val x = doubleArrayOf(0.0, 0.4, 1.0)
    val y = doubleArrayOf(0.0, 1.0, 1.0)

    return SplineInterpolator().interpolate(x, y)
  }

  private fun getKeywordsWIthFreq(source: CommentEntity, locale: Locale): Map<String, Double> {
    val text = source.text
    val nouns = partOfSpeechService.tag(text, locale)
      .filter { (_, tag) -> tag == "NOUN" || tag == "PROPN" }
    return nouns
      .map { (word, _) -> word.lowercase() }
      .let { stemmerService.stem(it, locale) }
      .groupBy { it }
      .mapValues { (_, v) -> v.size.toDouble() / nouns.size.toDouble() }
  }

}
