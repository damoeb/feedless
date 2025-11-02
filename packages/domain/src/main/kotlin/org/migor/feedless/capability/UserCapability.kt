package org.migor.feedless.capability

import com.google.gson.Gson
import org.migor.feedless.user.UserId

open class UserCapability(val userId: UserId) : Capability<UserId>(ID, userId) {
  companion object {
    fun fromString(value: String): UserCapability {
      return UserCapability(Gson().fromJson(value, UserId::class.java))
    }

    val ID: String = "user";
  }
}
