package org.migor.rss.rich

import java.security.MessageDigest
import javax.xml.bind.DatatypeConverter

object CryptUtil {
  fun sha1(input: String) = hashString("SHA-1", input)

  private fun hashString(type: String, input: String): String {
    val bytes = MessageDigest
      .getInstance(type)
      .digest(input.toByteArray())
    return DatatypeConverter.printHexBinary(bytes).toUpperCase()
  }
}
