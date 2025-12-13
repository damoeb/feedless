package org.migor.feedless.data.jpa.annotation

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.annotation.Vote
import org.migor.feedless.annotation.VoteRepository
import org.migor.feedless.document.DocumentId
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.user.UserId
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("${AppProfiles.annotation} & ${AppLayer.repository}")
class VoteJpaRepository(private val voteDAO: VoteDAO) : VoteRepository {

  override fun existsByFlagAndUpVoteAndDownVoteAndOwnerIdAndRepositoryIdAndDocumentId(
    flag: Boolean,
    upvote: Boolean,
    downvote: Boolean,
    userId: UserId,
    documentId: DocumentId?,
    repositoryId: RepositoryId?
  ): Boolean {
    return voteDAO.existsByFlagAndUpVoteAndDownVoteAndOwnerIdAndRepositoryIdAndDocumentId(
      flag,
      upvote,
      downvote,
      userId.uuid,
      documentId?.uuid,
      repositoryId?.uuid
    )
  }

  override fun countUpVotesByRepositoryId(repositoryId: RepositoryId): Int {
    return voteDAO.countUpVotesIsTrueByRepositoryId(repositoryId.uuid)
  }

  override fun countDownVoteByRepositoryId(repositoryId: RepositoryId): Int {
    return voteDAO.countDownVoteIsTrueByRepositoryId(repositoryId.uuid)
  }

  override fun findAllByOwnerIdAndRepositoryId(userId: UserId, repositoryId: RepositoryId): List<Vote> {
    return voteDAO.findAllByOwnerIdAndRepositoryId(userId.uuid, repositoryId.uuid).map { it.toDomain() }
  }

  override fun findAllByOwnerIdAndDocumentId(userId: UserId, documentId: DocumentId): List<Vote> {
    return voteDAO.findAllByOwnerIdAndDocumentId(userId.uuid, documentId.uuid).map { it.toDomain() }
  }

  override fun save(vote: Vote): Vote {
    return voteDAO.save(vote.toEntity()).toDomain()
  }
}

