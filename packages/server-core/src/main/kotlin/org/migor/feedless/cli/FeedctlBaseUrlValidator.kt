package org.migor.feedless.cli

import java.net.URI
import java.net.URISyntaxException

/**
 * install.sh substitutes CliInstallScriptController.feedctlBaseUrlPlaceholder
 * directly into a double-quoted shell assignment
 * (`FEEDCTL_BASE_URL="${FEEDCTL_BASE_URL:-<substituted>}"`), and every
 * `curl .../cli/install.sh | sh` runs the result. `PropertyService` only
 * checks that `apiGatewayUrl` is non-empty, so a careless env var value
 * (e.g. one holding a stray `"`, a backtick, or a `$(...)`) is enough to
 * break out of that assignment and run arbitrary shell for every
 * downloader -- no attacker required. This is the single gate
 * CliInstallScriptController must pass before templating install.sh.
 */
object FeedctlBaseUrlValidator {

  // Rules out quotes, backticks, `$`, backslashes, whitespace, `;`, and
  // anything else that could break out of the double-quoted shell string
  // install.sh substitutes this value into.
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
