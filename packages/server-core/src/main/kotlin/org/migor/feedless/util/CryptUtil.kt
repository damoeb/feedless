package org.migor.feedless.util

import org.apache.commons.lang3.StringUtils
import java.security.MessageDigest
import java.util.*
import javax.xml.bind.DatatypeConverter

object CryptUtil {
  fun sha1(input: String) = hashString("SHA-1", input.toByteArray())
  fun sha1(input: ByteArray) = hashString("SHA-1", input)

  private fun hashString(type: String, input: ByteArray): String {
    val bytes = MessageDigest
      .getInstance(type)
      .digest(input)
    return DatatypeConverter.printHexBinary(bytes).uppercase(Locale.getDefault())
  }

  fun newCorrId(length: Int = 4, parentCorrId: String? = null): String {
    val charset = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    val corrId = (1..length)
      .map { charset.random() }
      .joinToString("")
    return parentCorrId?.let { "$it/$corrId" } ?: corrId
  }

  fun handleCorrId(corrId: String?): String {
    return StringUtils.abbreviate(corrId ?: newCorrId(), 5)
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
