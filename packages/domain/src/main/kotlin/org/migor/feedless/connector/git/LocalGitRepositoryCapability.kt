package org.migor.feedless.connector.git

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class LocalGitRepositoryCapability(
  @Contextual val directory: File,
  val gitConnectionCapability: GitConnectionCapability
)
