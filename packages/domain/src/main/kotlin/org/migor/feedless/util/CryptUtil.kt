package org.migor.feedless.util

import java.security.MessageDigest
import java.util.*

object CryptUtil {
  fun sha1(input: String) = hashString(input.toByteArray())
  fun sha1(input: ByteArray) = hashString(input)

  private fun hashString(input: ByteArray): String {
    val bytes = MessageDigest
      .getInstance("SHA-1")
      .digest(input)
    return HexFormat.of().formatHex(bytes).uppercase(Locale.getDefault())
  }

  fun newCorrId(length: Int = 4, parentCorrId: String? = null): String {
    val charset = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    val corrId = (1..length)
      .map { charset.random() }
      .joinToString("")
    return parentCorrId?.let { "$it/$corrId" } ?: corrId
  }

//  fun extractDigest(authorization: String?): String? {
//    if (authorization != null && authorization.lowercase().startsWith("digest")) {
//      val digest = authorization.split(" ")[1]
//      if (StringUtils.isNotBlank(digest)) {
//        return digest
//      }
//    }
//    return null
//  }

}
