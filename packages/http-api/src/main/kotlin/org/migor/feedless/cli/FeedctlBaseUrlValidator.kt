package org.migor.feedless.cli

import java.net.URI
import java.net.URISyntaxException

/** Gate for the URL templated into install.sh's shell assignment: a stray quote or $(...) would run shell on every install. */
object FeedctlBaseUrlValidator {

  // Nothing that could break out of the double-quoted shell string.
  private val allowedChars = Regex("^[A-Za-z0-9._~:/-]+$")

  fun isValid(url: String): Boolean {
    if (!allowedChars.matches(url)) {
      return false
    }

    val uri = try {
      URI(url)
    } catch (e: URISyntaxException) {
      return false
    }

    val scheme = uri.scheme?.lowercase()
    return (scheme == "http" || scheme == "https") &&
      !uri.host.isNullOrBlank() &&
      uri.userInfo == null &&
      uri.rawQuery == null &&
      uri.rawFragment == null
  }
}
