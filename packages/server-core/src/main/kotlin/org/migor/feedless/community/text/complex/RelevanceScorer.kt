package org.migor.feedless.community.text.complex

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.community.CommentGraphService
import org.migor.feedless.community.text.simple.KeywordIntersectionScorer
import org.migor.feedless.data.jpa.comment.CommentEntity
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

data class RelevanceWeights(val context: Double)

@Service
@Profile("${AppProfiles.community} & ${AppLayer.service}")
class RelevanceScorer(
  private val keywordIntersectionScorer: KeywordIntersectionScorer,
  private val commentGraphService: CommentGraphService
) {

  private val log = LoggerFactory.getLogger(RelevanceScorer::class.simpleName)

  suspend fun relevance(comment: CommentEntity, w: RelevanceWeights): Double {
    /*
Keyword Analysis: Let K be a score indicating the presence of relevant keywords or phrases.
Contextual Understanding: Let CU be a score indicating the contextual understanding of the content.
 */
    return commentGraphService.getParent(comment)
      ?.let { parent -> w.context * keywordIntersectionScorer.score(parent, comment) }
      ?: 1.0
  }
}


