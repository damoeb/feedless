package org.migor.feedless

import java.time.Duration

open class HarvestException(override val message: String) : RuntimeException()
class PermissionDeniedException(override val message: String): RuntimeException()
class BadRequestException(override val message: String): RuntimeException()

class UnavailableException(override val message: String): RuntimeException()
class NotFoundException(override val message: String): RuntimeException()
class SiteNotFoundException : HarvestException("site not found")
class MethodNotAllowedException(corrId: String) : HarvestException("method not allowed ($corrId)")
class ServiceUnavailableException(corrId: String) : HarvestException("site unavailable ($corrId)")
class HarvestAbortedException(corrId: String, message: String) : HarvestException("$message ($corrId)")
open class ResumableHarvestException(corrId: String, message: String, val nextRetryAfter: Duration) : HarvestException("$message ($corrId)")
class HostOverloadingException(corrId: String, message: String, waitForRefill: Duration) : ResumableHarvestException(corrId, message, waitForRefill)
class TemporaryServerException(corrId: String, message: String, waitForRefill: Duration) : ResumableHarvestException(corrId, message, waitForRefill)
