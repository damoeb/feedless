package org.migor.rich.rss.harvest

open class HarvestException(override val message: String) : RuntimeException()
class SiteNotFoundException : HarvestException("site not found")
class ServiceUnavailableException : HarvestException("site unavailable")
class BlacklistedForSiteHarvestException(message: String) : HarvestException(message)
