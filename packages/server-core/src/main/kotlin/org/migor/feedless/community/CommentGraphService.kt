package org.migor.feedless.community

import org.migor.feedless.AppProfiles
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service


@Service
@Profile(AppProfiles.community)
class CommentGraphService {
  fun getReplyCount(comment: CommentEntity): Int {
    return 0
  }

  fun getParent(comment: CommentEntity): CommentEntity? {
    return null
  }


}
