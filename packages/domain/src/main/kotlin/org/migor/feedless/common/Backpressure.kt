package org.migor.feedless.common

import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

/** Marks work that is retried later anyway, so a throttled request fails fast instead of waiting. */
object Backpressure : CoroutineContext.Element, CoroutineContext.Key<Backpressure> {
  override val key: CoroutineContext.Key<*> get() = this
}

suspend fun handlesBackpressure(): Boolean = coroutineContext[Backpressure] != null
