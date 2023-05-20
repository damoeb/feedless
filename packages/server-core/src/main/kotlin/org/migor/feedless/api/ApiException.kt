package org.migor.feedless.api

import com.google.gson.GsonBuilder
import com.google.gson.annotations.Expose


enum class ApiErrorCode(val code: Int) {
  INTERNAL_ERROR(2),
}

class ApiException(@Expose val errorCode: ApiErrorCode, @Expose val errorMessage: String?) : RuntimeException() {
  fun toJson(): String {
    return GsonBuilder()
      .excludeFieldsWithoutExposeAnnotation()
      .create()
      .toJson(this)
  }

  override fun toString(): String {
    return toJson()
  }
}
