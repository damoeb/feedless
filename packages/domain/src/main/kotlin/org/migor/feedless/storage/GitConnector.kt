package org.migor.feedless.storage

interface GitConnector {
  fun connect(connectionConfig: GitConnectionConfig): GitConnectionHandle
}
