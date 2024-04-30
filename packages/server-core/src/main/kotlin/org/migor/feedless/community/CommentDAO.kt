package org.migor.feedless.community

import org.migor.feedless.AppProfiles
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
@Profile(AppProfiles.community)
interface CommentDAO : JpaRepository<CommentEntity, UUID> {

}
