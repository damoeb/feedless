package org.migor.feedless.api.graphql

import org.migor.feedless.generated.types.ServerSettingsContextInput
import org.springframework.cache.interceptor.KeyGenerator
import java.lang.reflect.Method

class CacheKeyGenerator : KeyGenerator {
  override fun generate(target: Any, method: Method, vararg params: Any?): Any {
    val ex = IllegalArgumentException()
    return when (method.name) {
      "serverSettings" -> if (params[0] is ServerSettingsContextInput) method.name + "/" + (params[0] as ServerSettingsContextInput).host else throw ex
      else -> throw ex
    }
  }
}
