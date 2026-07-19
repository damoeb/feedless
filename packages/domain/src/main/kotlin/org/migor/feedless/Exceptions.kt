package org.migor.feedless

open class FatalHarvestException(override val message: String) : RuntimeException()
class PermissionDeniedException(override val message: String) : FatalHarvestException(message)
class NotFoundException(override val message: String) : RuntimeException()
