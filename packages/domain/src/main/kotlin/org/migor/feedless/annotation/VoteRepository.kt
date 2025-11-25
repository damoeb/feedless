package org.migor.feedless.annotation

import org.migor.feedless.document.DocumentId
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.user.UserId

interface VoteRepository {

    suspend fun existsByFlagAndUpVoteAndDownVoteAndOwnerIdAndRepositoryIdAndDocumentId(
        flag: Boolean,
        upvote: Boolean,
        downvote: Boolean,
        userId: UserId,
        documentId: DocumentId?,
        repositoryId: RepositoryId?
    ): Boolean

    suspend fun countUpVotesIsTrueByRepositoryId(repositoryId: RepositoryId): Int

    suspend fun countDownVoteIsTrueByRepositoryId(repositoryId: RepositoryId): Int

    suspend fun findAllByOwnerIdAndRepositoryId(userId: UserId, repositoryId: RepositoryId): List<Vote>

    suspend fun findAllByOwnerIdAndDocumentId(userId: UserId, documentId: DocumentId): List<Vote>

    suspend fun save(vote: Vote): Vote
}

