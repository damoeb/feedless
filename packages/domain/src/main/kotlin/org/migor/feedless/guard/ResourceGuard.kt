package org.migor.feedless.guard

interface ResourceGuard<T, E> {

  suspend fun requireRead(id: T): E

  suspend fun requireWrite(id: T): E

  suspend fun requireExecute(id: T): E
}
