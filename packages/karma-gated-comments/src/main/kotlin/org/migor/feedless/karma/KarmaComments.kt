package org.migor.feedless.karma

import org.migor.feedless.document.DocumentId
import org.migor.feedless.user.UserId
import org.springframework.stereotype.Component

/**
 * Handles karma events for actions defined in karma-comments-idea.yaml.
 * Each method corresponds to an action and receives actor and context IDs.
 */
@Component
class KarmaComments(
  private val karmaHandlerFactory: KarmaHandlerFactory,
  private val documentHandlerFactory: DocumentHandlerFactory
) {

  private val rules =
    mapOf(
      KarmaChangeReason.UPVOTE_GIVEN to mapOf(
        KarmaSubject.AUTHOR to 4,
        KarmaSubject.AGENT to -1
      )
    )

  fun on(agentId: UserId, documentId: DocumentId, reason: KarmaChangeReason) {
    val rule = rules[reason]!!

    rule[KarmaSubject.AUTHOR]?.let { karmaChange ->
      changeUserKarma(
        getAuthor(documentId),
        karmaChange,
        documentId,
        reason,
      )
    }

    rule[KarmaSubject.AGENT]?.let { karmaChange ->
      changeUserKarma(
        agentId,
        karmaChange,
        documentId,
        reason,
      )
    }

    updateDocumentScore(documentId);
  }

  fun onUpvote(actorId: UserId, documentId: DocumentId) {
    // TODO: resolve author(documentId), apply karma_change for author +4 (given daily cap), actor -1

//    documentHandlerFactory.from(documentId).totalKarmaSince(Duration.ofDays(1))
//    karmaHandlerFactory.from(actorId).totalKarmaSince(Duration.ofDays(1))

    changeUserKarma(getAuthor(documentId), 4, documentId, KarmaChangeReason.UPVOTE_RECEIVED)
    changeUserKarma(actorId, -1, documentId, KarmaChangeReason.UPVOTE_GIVEN)
    updateDocumentScore(documentId);
  }

//  fun afterCommentUpvote(actorId: UserId, commentId: DocumentId) {
//    // TODO: resolve author(commentId), apply karma_change for author +4 (given daily cap), actor -1
//  }

  fun onDownvote(actorId: UserId, documentId: DocumentId) {
    // TODO: resolve author(documentId), apply karma_change for author -4 (given daily cap)
    changeUserKarma(getAuthor(documentId), 4, documentId, KarmaChangeReason.DOWNVOTE_RECEIVED)
    changeUserKarma(actorId, -1, documentId, KarmaChangeReason.DOWNVOTE_GIVEN)
    updateDocumentScore(documentId);
  }

//  fun afterCommentDownvote(actorId: UserId, documentId: DocumentId, commentId: DocumentId) {
//    // TODO: resolve author(commentId), apply karma_change for author -2
//  }

//  fun onPostFlagged(actorId: UserId, documentId: DocumentId) {
//    // TODO: resolve author(documentId), apply karma_change for author -10
//    changeUserKarma(getAuthor(documentId), 4, documentId, "downvote received")
//  }

  fun onFlagged(actorId: UserId, documentId: DocumentId) {
    // TODO: resolve author(documentId), apply karma_change for author -10
    changeUserKarma(getAuthor(documentId), -10, documentId, KarmaChangeReason.FLAG_RECEIVED)
    changeUserKarma(actorId, -1, documentId, KarmaChangeReason.FLAG_GIVEN)
    updateDocumentScore(documentId);
  }

  fun onConfirmedSpam(documentId: DocumentId) {
    // TODO: karma_change for user -50, suspend_user until 7 days from now

    changeUserKarma(getAuthor(documentId), -10, documentId, KarmaChangeReason.SPAM)
    updateDocumentScore(documentId);
  }

  fun onPostDeletedByAuthor(actorId: UserId, documentId: DocumentId) {
    // TODO: karma_change for actor -5
    changeUserKarma(getAuthor(documentId), -5, documentId, KarmaChangeReason.COMMENTS_DELETED)
  }

  private fun changeUserKarma(
    affectedByKarmaChange: UserId,
    karmaChange: Int,
    documentId: DocumentId,
    reason: KarmaChangeReason
  ) {
    karmaHandlerFactory.from(affectedByKarmaChange)
      .changeKarma(karmaChange, reason, documentId)
  }

  private fun getAuthor(documentId: DocumentId): UserId {
    return documentHandlerFactory.from(documentId)
      .authorId()
  }

  private fun updateDocumentScore(documentId: DocumentId) {
    documentHandlerFactory.from(documentId).updateScore()
  }

}
