package org.migor.feedless.connector.git

interface GitConnector {
  fun connect(connectionConfig: GitConnectionConfig): GitConnectionHandle
}
