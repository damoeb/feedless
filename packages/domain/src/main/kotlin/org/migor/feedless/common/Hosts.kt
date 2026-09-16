package org.migor.feedless.common

import java.net.URI

/** The key cooldowns are stored under; must match the host expression in SourceDAO.findDueForHarvest. */
fun hostOf(url: String): String? {
  val withScheme = if (Regex("^[a-zA-Z][a-zA-Z0-9+.-]*://").containsMatchIn(url)) url else "https://$url"
  return runCatching { URI(withScheme).host }.getOrNull()?.lowercase()?.takeIf { it.isNotBlank() }
}
