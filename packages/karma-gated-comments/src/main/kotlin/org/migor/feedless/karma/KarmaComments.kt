package org.migor.feedless.karma

import org.migor.feedless.document.DocumentId
import org.migor.feedless.user.UserId
import org.springframework.stereotype.Service

/**
 * Handles karma events for actions defined in karma-comments-idea.yaml.
 * Each method corresponds to an action and receives actor and context IDs.
 */
@Service
class KarmaComments {

  fun onUpvote(actorId: UserId, documentId: DocumentId) {
    // TODO: resolve author(documentId), apply karma_change for author +4 (given daily cap), actor -1

    changeUserKarma(getAuthor(documentId), 4, documentId, "upvote received")
    changeUserKarma(actorId, -1, documentId, "upvote given")
    updateDocumentScore(documentId);
  }

//  fun afterCommentUpvote(actorId: UserId, commentId: DocumentId) {
//    // TODO: resolve author(commentId), apply karma_change for author +4 (given daily cap), actor -1
//  }

  fun onDownvote(actorId: UserId, documentId: DocumentId) {
    // TODO: resolve author(documentId), apply karma_change for author -4 (given daily cap)
    changeUserKarma(getAuthor(documentId), 4, documentId, "downvote received")
    changeUserKarma(actorId, -1, documentId, "downvote given")
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
    changeUserKarma(getAuthor(documentId), -10, documentId, "flag received")
    changeUserKarma(actorId, -1, documentId, "flag given")
    updateDocumentScore(documentId);
  }

  fun onConfirmedSpam(documentId: DocumentId) {
    // TODO: karma_change for user -50, suspend_user until 7 days from now

    changeUserKarma(getAuthor(documentId), -10, documentId, "flag received")
    updateDocumentScore(documentId);
  }

  fun onPostDeletedByAuthor(actorId: UserId, documentId: DocumentId) {
    // TODO: karma_change for actor -5
    changeUserKarma(getAuthor(documentId), -5, documentId, "comments deleted")
  }

  fun onDailyActivityBonus(userId: UserId) {
    // TODO: karma_change for user +1 given lastLogin is today
  }

  private fun changeUserKarma(
    affectedByKarmaChange: UserId,
    karmaChange: Int,
    documentId: DocumentId,
    action: String
  ) {
    val karmaHandler = karmaHandlerFactory.from(affectedByKarmaChange)

    karmaHandler.changeKarma(affectedByKarmaChange)

    val documentHandler = documentHandlerFactory.from(affectedByKarmaChange)

  }

  private fun getAuthor(documentId: DocumentId): UserId {
    return documentHandlerFactory.from(documentId)
      .ownerId()
  }

  private fun updateDocumentScore(documentId: DocumentId) {
    TODO("Not yet implemented")
  }

}
