package org.migor.feedless.capability

import kotlinx.serialization.Serializable


@Serializable
open class Capability<T>(open val capabilityId: CapabilityId, open val capabilityPayload: T)
