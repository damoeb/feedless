package org.migor.feedless.community.text.simple

import org.apache.commons.math3.analysis.interpolation.SplineInterpolator
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction
import org.migor.feedless.AppProfiles
import org.migor.feedless.community.CommentEntity
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.util.regex.Pattern

@Service
@Profile(AppProfiles.community)
class NoveltyScorer {

  private val log = LoggerFactory.getLogger(KeywordIntersectionScorer::class.simpleName)
  private var spline: PolynomialSplineFunction = createSplineInterpolator()

  fun score(comment: CommentEntity): Double {
    return spline.value(getHyperLinks(comment.contentText).size.toDouble())
  }

  private fun createSplineInterpolator(): PolynomialSplineFunction {
    val x = doubleArrayOf(0.0, 3.0, 4.0, 5.0)
    val y = doubleArrayOf(0.0, 1.0, 1.0, 1.0)

    return SplineInterpolator().interpolate(x, y)
  }
}


private val urlPattern: Pattern = Pattern.compile(
  "(?:^|[\\W])((ht|f)tp(s?):\\/\\/|www\\.)"
    + "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*"
    + "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)",
  Pattern.CASE_INSENSITIVE or Pattern.MULTILINE or Pattern.DOTALL
)

fun getHyperLinks(s: String): MutableList<String> {
  val urls = mutableListOf<String>()
  val urlMatcher = urlPattern.matcher(s)
  while (urlMatcher.find()) {
    val matchStart = urlMatcher.start(1)
    val matchEnd = urlMatcher.end()
    urls.add(s.substring(matchStart, matchEnd))
  }
  return urls
}
