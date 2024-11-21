package org.migor.feedless

import java.time.Duration

open class FatalHarvestException(override val message: String) : RuntimeException()
class PermissionDeniedException(override val message: String) : FatalHarvestException(message)
class BadRequestException(override val message: String) : FatalHarvestException(message)

class UnavailableException(override val message: String) : ResumableHarvestException(message, Duration.ofMinutes(5))
class NotFoundException(override val message: String) : RuntimeException()
class SiteNotFoundException(url: String) : FatalHarvestException("$url not found")
open class ResumableHarvestException(message: String, val nextRetryAfter: Duration) :
  RuntimeException(message)

class HostOverloadingException(message: String, waitForRefill: Duration) :
  ResumableHarvestException(message, waitForRefill)

class NoItemsRetrievedException : RuntimeException("no items retireved")

class TemporaryServerException(message: String, waitForRefill: Duration) :
  ResumableHarvestException(message, waitForRefill)
