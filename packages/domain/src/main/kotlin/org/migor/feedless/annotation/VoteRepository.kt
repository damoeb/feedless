package org.migor.feedless.annotation

import org.migor.feedless.document.DocumentId
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.user.UserId

interface VoteRepository {

  fun existsByFlagAndUpVoteAndDownVoteAndOwnerIdAndRepositoryIdAndDocumentId(
    flag: Boolean,
    upvote: Boolean,
    downvote: Boolean,
    userId: UserId,
    documentId: DocumentId?,
    repositoryId: RepositoryId?
  ): Boolean

  fun countUpVotesByRepositoryId(repositoryId: RepositoryId): Int

  fun countDownVoteByRepositoryId(repositoryId: RepositoryId): Int

  fun findAllByOwnerIdAndRepositoryId(userId: UserId, repositoryId: RepositoryId): List<Vote>

  fun findAllByOwnerIdAndDocumentId(userId: UserId, documentId: DocumentId): List<Vote>

  fun save(vote: Vote): Vote
}

