package org.migor.feedless.api.mapper

import org.mapstruct.MapperConfig
import org.mapstruct.ReportingPolicy

/**
 * Shared MapStruct configuration for all mappers
 */
@MapperConfig(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
interface MapStructConfig
