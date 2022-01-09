package org.migor.rss.rich.api

import com.google.gson.GsonBuilder
import com.google.gson.annotations.Expose


enum class ApiErrorCode(val code: Int) {
  UNKNOWN_FEED_FORMAT(1),
  INTERNAL_ERROR(2),
  UNAUTHORIZED(3)
}

class ApiException(@Expose val errorCode: ApiErrorCode, @Expose val errorMessage: String?) : RuntimeException() {
  fun toJson(): String {
    return GsonBuilder()
      .excludeFieldsWithoutExposeAnnotation()
      .create()
      .toJson(this)
  }
}
