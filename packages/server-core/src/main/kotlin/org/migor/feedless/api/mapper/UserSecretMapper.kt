package org.migor.feedless.api.mapper

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.migor.feedless.util.toMillis
import org.migor.feedless.generated.types.UserSecret as UserSecretDto

/**
 * MapStruct mapper for UserSecret
 */
@Mapper(config = MapStructConfig::class, uses = [EnumMapper::class])
abstract class UserSecretMapper {

  @Mapping(target = "id", expression = "java(userSecret.getId().getUuid().toString())")
  @Mapping(target = "type", source = "userSecret.type")
  @Mapping(target = "value", expression = "java(maskValue(userSecret.getValue(), mask))")
  @Mapping(target = "valueMasked", source = "mask")
  @Mapping(target = "validUntil", expression = "java(MapperUtil.toMillis(userSecret.getValidUntil()))")
  @Mapping(target = "lastUsed", expression = "java(MapperUtil.toMillis(userSecret.getLastUsedAt()))")
  abstract fun toDto(userSecret: org.migor.feedless.userSecret.UserSecret, mask: Boolean = true): UserSecretDto

  protected fun maskValue(value: String, mask: Boolean): String {
    return if (mask && value.length > 5) {
      value.substring(0..4) + "****"
    } else {
      value
    }
  }
}

