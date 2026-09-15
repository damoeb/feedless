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

  // A JWT's prefix is the same for every token; only its signature end tells tokens apart.
  protected fun maskValue(value: String, mask: Boolean): String {
    return when {
      !mask -> value
      value.length > 2 * VISIBLE_SUFFIX_LENGTH -> MASK + value.takeLast(VISIBLE_SUFFIX_LENGTH)
      else -> MASK
    }
  }

  companion object {
    private const val MASK = "••••"
    private const val VISIBLE_SUFFIX_LENGTH = 6
  }
}

