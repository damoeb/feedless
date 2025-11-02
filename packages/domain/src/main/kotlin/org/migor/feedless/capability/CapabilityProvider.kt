package org.migor.feedless.capability

interface CapabilityProvider<T> {
  fun capability(): Capability<T>
}
