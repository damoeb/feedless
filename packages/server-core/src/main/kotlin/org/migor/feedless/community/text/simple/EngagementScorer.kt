package org.migor.feedless.community.text.simple

import org.apache.commons.math3.analysis.interpolation.SplineInterpolator
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.comment.CommentEntity
import org.migor.feedless.community.CommentGraphService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.community} & ${AppLayer.service}")
class EngagementScorer(private val commentGraphService: CommentGraphService) {

  private val log = LoggerFactory.getLogger(KeywordIntersectionScorer::class.simpleName)

  private var spline: PolynomialSplineFunction = createSplineInterpolator()

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
