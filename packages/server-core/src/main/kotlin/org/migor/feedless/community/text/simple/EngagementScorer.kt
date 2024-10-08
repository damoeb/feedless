package org.migor.feedless.community.text.simple

import org.apache.commons.math3.analysis.interpolation.SplineInterpolator
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction
import org.migor.feedless.AppProfiles
import org.migor.feedless.community.CommentEntity
import org.migor.feedless.community.CommentGraphService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile(AppProfiles.community)
class EngagementScorer {

  private val log = LoggerFactory.getLogger(KeywordIntersectionScorer::class.simpleName)

  private var spline: PolynomialSplineFunction = createSplineInterpolator()

  @Autowired
  lateinit var commentGraphService: CommentGraphService

  suspend fun score(comment: CommentEntity): Double {
    return spline.value(commentGraphService.getReplyCount(comment).toDouble())
      .coerceIn(0.0, 1.0)
  }

  private fun createSplineInterpolator(): PolynomialSplineFunction {
    val x = doubleArrayOf(0.0, 4.0, 10.0, 20.0)
    val y = doubleArrayOf(0.0, 1.0, 0.5, 0.0)

    return SplineInterpolator().interpolate(x, y)
  }

}
