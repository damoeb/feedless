package org.migor.feedless

import java.time.Duration

open class FatalHarvestException(override val message: String) : RuntimeException()
class PermissionDeniedException(override val message: String) : FatalHarvestException(message)
class NotFoundException(override val message: String) : RuntimeException()

/** The resource's current state does not allow the request — mapped to 409 at the HTTP edge. */
class ConflictException(override val message: String) : RuntimeException()

/**
 * Rate limit exhausted. Thrown rather than swallowed so callers get a 429 (HTTP) or an
 * explicit error (GraphQL) instead of a silent null.
 */
class TooManyRequestsException(
  override val message: String = "rate limit exceeded",
  val retryAfter: Duration = Duration.ofMinutes(1),
) : RuntimeException()

/** A failure worth retrying after [nextRetryAfter]. */
open class ResumableHarvestException(message: String, val nextRetryAfter: Duration) :
  RuntimeException(message)

/** Rate limit exhausted — raised by the throttle layer, mapped to 429 at the HTTP edge. */
class HostOverloadingException(message: String, waitForRefill: Duration) :
  ResumableHarvestException(message, waitForRefill)
