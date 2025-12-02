package org.migor.feedless.community

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.comment.CommentEntity
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.community} & ${AppLayer.service}")
class CommentGraphService {
  suspend fun getReplyCount(comment: CommentEntity): Int {
    return 0
  }

  suspend fun getParent(comment: CommentEntity): CommentEntity? {
    return null
  }


}
