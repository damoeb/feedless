package org.migor.feedless.harvest

import java.time.Duration

open class HarvestException(override val message: String) : RuntimeException()
class SiteNotFoundException : HarvestException("site not found")
class MethodNotAllowedException : HarvestException("method not allowed")
class ServiceUnavailableException : HarvestException("site unavailable")
class HarvestAbortedException(message: String) : HarvestException(message)

open class ResumableHarvestException(message: String, val nextRetryAfter: Duration) : HarvestException(message)
class HostOverloadingException(message: String, waitForRefill: Duration) : ResumableHarvestException(message, waitForRefill)
class TemporaryServerException(message: String, waitForRefill: Duration) : ResumableHarvestException(message, waitForRefill)
