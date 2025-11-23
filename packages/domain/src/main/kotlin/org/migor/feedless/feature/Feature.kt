package org.migor.feedless.feature

import java.time.LocalDateTime
import java.util.*

data class Feature(
    val id: FeatureId = FeatureId(UUID.randomUUID()),
    val name: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
)

