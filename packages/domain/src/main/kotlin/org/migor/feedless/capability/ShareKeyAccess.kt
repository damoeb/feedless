package org.migor.feedless.capability

import org.migor.feedless.repository.RepositoryId
import kotlin.coroutines.CoroutineContext

/** A share key presented for [repositoryId]; [org.migor.feedless.repository.RepositoryGuard] decides whether it opens that repository. */
class ShareKeyAccess(
  val repositoryId: RepositoryId,
  val shareKey: String,
) : CoroutineContext.Element {
  companion object Key : CoroutineContext.Key<ShareKeyAccess>

  override val key: CoroutineContext.Key<*> = Key

  // the key is a secret, keep it out of logs
  override fun toString(): String = "ShareKeyAccess(repositoryId=$repositoryId)"
}
