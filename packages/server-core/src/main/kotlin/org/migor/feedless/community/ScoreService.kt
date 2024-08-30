package org.migor.feedless.community

import org.migor.feedless.AppProfiles
import org.migor.feedless.community.text.complex.CivilityScorer
import org.migor.feedless.community.text.complex.CivilityWeights
import org.migor.feedless.community.text.complex.OriginalityScorer
import org.migor.feedless.community.text.complex.OriginalityWeights
import org.migor.feedless.community.text.complex.QualityScorer
import org.migor.feedless.community.text.complex.QualityWeights
import org.migor.feedless.community.text.complex.RelevanceScorer
import org.migor.feedless.community.text.complex.RelevanceWeights
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

data class ScoreWeights(
  val civility: CivilityWeights,
  val quality: QualityWeights,
  val relevance: RelevanceWeights,
  val originality: OriginalityWeights,
)

@Service
@Profile(AppProfiles.community)
class ScoreService {

  private val log = LoggerFactory.getLogger(ScoreService::class.simpleName)

  @Autowired
  private lateinit var civilityScorer: CivilityScorer

  @Autowired
  private lateinit var qualityScorer: QualityScorer

  @Autowired
  private lateinit var relevanceScorer: RelevanceScorer

  @Autowired
  private lateinit var originalityScorer: OriginalityScorer

  suspend fun score(comment: CommentEntity, weights: ScoreWeights): Double {
    return arrayOf(
      civilityScorer.civility(comment, weights.civility),
      qualityScorer.quality(comment, weights.quality),
      relevanceScorer.relevance(comment, weights.relevance),
      originalityScorer.originality(comment, weights.originality)
    ).average()
  }

}
