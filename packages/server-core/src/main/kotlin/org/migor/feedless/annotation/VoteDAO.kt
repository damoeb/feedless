package org.migor.feedless.annotation

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
@Profile("${AppProfiles.annotation} & ${AppLayer.repository}")
interface VoteDAO: JpaRepository<VoteEntity, UUID> {

  fun existsByFlagAndUpVoteAndDownVoteAndOwnerIdAndRepositoryIdAndDocumentId(
    flag: Boolean,
    upvote: Boolean,
    downvote: Boolean,
    userId: UUID,
    documentId: UUID?,
    repositoryId: UUID?
  ): Boolean

  fun countUpVotesIsTrueByRepositoryId(repositoryId: UUID): Int
  fun countDownVoteIsTrueByRepositoryId(repositoryId: UUID): Int
  fun findAllByOwnerIdAndRepositoryId(userId: UUID, repositoryId: UUID): List<VoteEntity>
}
