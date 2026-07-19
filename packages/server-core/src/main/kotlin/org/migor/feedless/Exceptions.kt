package org.migor.feedless

import java.time.Duration

class BadRequestException(override val message: String) : FatalHarvestException(message)

class UnavailableException(override val message: String) : ResumableHarvestException(message, Duration.ofMinutes(5))
class SiteNotFoundException(url: String) : FatalHarvestException("$url not found")
open class ResumableHarvestException(message: String, val nextRetryAfter: Duration) :
  RuntimeException(message)

class HostOverloadingException(message: String, waitForRefill: Duration) :
  ResumableHarvestException(message, waitForRefill)

class NoItemsRetrievedException : RuntimeException("no items retireved")

class TemporaryServerException(message: String, waitForRefill: Duration) :
  ResumableHarvestException(message, waitForRefill)
