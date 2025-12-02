package org.migor.feedless.community

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.community.text.complex.CivilityScorer
import org.migor.feedless.community.text.complex.CivilityWeights
import org.migor.feedless.community.text.complex.OriginalityScorer
import org.migor.feedless.community.text.complex.OriginalityWeights
import org.migor.feedless.community.text.complex.QualityScorer
import org.migor.feedless.community.text.complex.QualityWeights
import org.migor.feedless.community.text.complex.RelevanceScorer
import org.migor.feedless.community.text.complex.RelevanceWeights
import org.migor.feedless.data.jpa.comment.CommentEntity
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

data class ScoreWeights(
  val civility: CivilityWeights,
  val quality: QualityWeights,
  val relevance: RelevanceWeights,
  val originality: OriginalityWeights,
)

@Service
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.community} & ${AppLayer.service}")
class ScoreService(
  private val civilityScorer: CivilityScorer,
  private val qualityScorer: QualityScorer,
  private val relevanceScorer: RelevanceScorer,
  private val originalityScorer: OriginalityScorer
) {

  private val log = LoggerFactory.getLogger(ScoreService::class.simpleName)

  suspend fun score(comment: CommentEntity, weights: ScoreWeights): Double {
    return arrayOf(
      civilityScorer.civility(comment, weights.civility),
      qualityScorer.quality(comment, weights.quality),
      relevanceScorer.relevance(comment, weights.relevance),
      originalityScorer.originality(comment, weights.originality)
    ).average()
  }

}
