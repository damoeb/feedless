package org.migor.feedless.capability

open class AgentCapability(val dummy: String) : Capability<String>(ID, dummy) {
  companion object {
    val ID: String = "agent"
  }
}
