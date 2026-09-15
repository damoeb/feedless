package org.migor.feedless.pipeline

interface Plugin {
  fun id(): String

  fun name(): String

  fun listed(): Boolean
}
