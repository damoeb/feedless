package org.migor.feedless.util

import java.security.MessageDigest
import java.security.SecureRandom
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

  private val secureRandom = SecureRandom()
  private val keyAlphabet = ('a'..'z') + ('A'..'Z') + ('0'..'9')

  /** Share keys open private feeds without a login, so they come from a cryptographic source; same alphabet as before. */
  fun newShareKey(length: Int = 9): String =
    (1..length).map { keyAlphabet[secureRandom.nextInt(keyAlphabet.size)] }.joinToString("")

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
