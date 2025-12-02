package org.migor.feedless.community.text.complex

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.comment.CommentEntity
import org.migor.feedless.community.text.simple.DuplicateContentScorer
import org.migor.feedless.community.text.simple.NoveltyScorer
import org.migor.feedless.community.text.simple.SpamScorer
import org.migor.feedless.community.text.simple.getHyperLinks
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

data class OriginalityWeights(
  val duplicate: Double,
  val novelty: Double,
  val spam: Double,
  val links: Double
)


@Service
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.community} & ${AppLayer.service}")
class OriginalityScorer(
  private val noveltyScorer: NoveltyScorer,
  private val spamScorer: SpamScorer,
  private val duplicateContentScorer: DuplicateContentScorer
) {

  private val log = LoggerFactory.getLogger(OriginalityScorer::class.simpleName)


  fun originality(comment: CommentEntity, w: OriginalityWeights): Double {
    /*
    Duplicate Content Detection: Let D be a similarity score indicating the degree of similarity to existing content.
    Novelty Analysis: Let N be a uniqueness score indicating the uniqueness of the content.
     */
    return (w.novelty * noveltyScorer.score(comment) - w.links * scoreHyperLinksSpamming(comment) - w.spam * spamScorer.score(
      comment
    ) - w.duplicate * duplicateContentScorer.score(comment)).coerceIn(0.0, 1.0)
  }

  fun scoreHyperLinksSpamming(comment: CommentEntity): Double {
    return if (getHyperLinks(comment.text).size > 2) {
      1.0
    } else {
      0.0
    }
  }

}
