package org.migor.feedless

import java.time.Duration

class BadRequestException(override val message: String) : FatalHarvestException(message)

class UnavailableException(override val message: String) : ResumableHarvestException(message, Duration.ofMinutes(5))
class SiteNotFoundException(url: String) : FatalHarvestException("$url not found")

// ResumableHarvestException and HostOverloadingException live in :packages:domain so http-api can map them.

class NoItemsRetrievedException : RuntimeException("no items retireved")

class TemporaryServerException(message: String, waitForRefill: Duration) :
  ResumableHarvestException(message, waitForRefill)
