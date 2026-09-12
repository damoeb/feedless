package org.migor.feedless.common

import org.migor.feedless.generated.types.ServerSettingsContextInput
import org.springframework.cache.interceptor.KeyGenerator
import java.lang.reflect.Method

class CacheKeyGenerator : KeyGenerator {
  override fun generate(target: Any, method: Method, vararg params: Any?): Any {
    val ex = IllegalArgumentException()
    val data = params.filterIsInstance<ServerSettingsContextInput>().firstOrNull()
    return when (method.name) {
      "serverSettings" -> if (data == null) {
        throw ex
      } else {
        method.name + "/" + data.host
      }

      else -> throw ex
    }
  }
}
