package org.migor.feedless.data.jpa.annotation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

    override suspend fun existsByFlagAndUpVoteAndDownVoteAndOwnerIdAndRepositoryIdAndDocumentId(
        flag: Boolean,
        upvote: Boolean,
        downvote: Boolean,
        userId: UserId,
        documentId: DocumentId?,
        repositoryId: RepositoryId?
    ): Boolean {
        return withContext(Dispatchers.IO) {
            voteDAO.existsByFlagAndUpVoteAndDownVoteAndOwnerIdAndRepositoryIdAndDocumentId(
                flag,
                upvote,
                downvote,
                userId.uuid,
                documentId?.uuid,
                repositoryId?.uuid
            )
        }
    }

    override suspend fun countUpVotesIsTrueByRepositoryId(repositoryId: RepositoryId): Int {
        return withContext(Dispatchers.IO) {
            voteDAO.countUpVotesIsTrueByRepositoryId(repositoryId.uuid)
        }
    }

    override suspend fun countDownVoteIsTrueByRepositoryId(repositoryId: RepositoryId): Int {
        return withContext(Dispatchers.IO) {
            voteDAO.countDownVoteIsTrueByRepositoryId(repositoryId.uuid)
        }
    }

    override suspend fun findAllByOwnerIdAndRepositoryId(userId: UserId, repositoryId: RepositoryId): List<Vote> {
        return withContext(Dispatchers.IO) {
            voteDAO.findAllByOwnerIdAndRepositoryId(userId.uuid, repositoryId.uuid).map { it.toDomain() }
        }
    }

    override suspend fun findAllByOwnerIdAndDocumentId(userId: UserId, documentId: DocumentId): List<Vote> {
        return withContext(Dispatchers.IO) {
            voteDAO.findAllByOwnerIdAndDocumentId(userId.uuid, documentId.uuid).map { it.toDomain() }
        }
    }

    override suspend fun save(vote: Vote): Vote {
        return withContext(Dispatchers.IO) {
            voteDAO.save(vote.toEntity()).toDomain()
        }
    }
}

