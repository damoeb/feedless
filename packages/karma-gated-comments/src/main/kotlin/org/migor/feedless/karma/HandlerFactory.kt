package org.migor.feedless.karma

interface HandlerFactory<ID, T> {
  fun from(documentId: ID): T
}
