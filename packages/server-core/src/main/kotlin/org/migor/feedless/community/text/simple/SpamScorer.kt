package org.migor.feedless.community.text.simple

import org.migor.feedless.AppProfiles
import org.migor.feedless.community.CommentEntity
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile(AppProfiles.community)
class SpamScorer {

  private val log = LoggerFactory.getLogger(KeywordIntersectionScorer::class.simpleName)

  fun score(comment: CommentEntity): Double {
    return if(getHyperLinks(comment.content).size > 2) {
      1.0
    } else {
      0.0
    }
  }
}
