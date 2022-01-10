package org.migor.rich.rss.util

import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import java.security.MessageDigest
import java.util.*
import javax.xml.bind.DatatypeConverter

object CryptUtil {
  private val log = LoggerFactory.getLogger(CryptUtil::class.simpleName)

  fun sha1(input: String) = hashString("SHA-1", input)

  private fun hashString(type: String, input: String): String {
    val bytes = MessageDigest
      .getInstance(type)
      .digest(input.toByteArray())
    return DatatypeConverter.printHexBinary(bytes).uppercase(Locale.getDefault())
  }

  fun newCorrId(length: Int = 4, parentCorrId: String? = null): String {
    val charset = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    val corrId = (1..length)
      .map { charset.random() }
      .joinToString("")
    return Optional.ofNullable(parentCorrId).map { "$it/$corrId" }.orElse(corrId)
  }

  fun handleCorrId(correlationId: String?): String {
    return StringUtils.abbreviate(Optional.ofNullable(correlationId).orElse(newCorrId()), 5)
  }

  fun extractDigest(authorization: String?): String? {
    if (authorization!=null && authorization.lowercase().startsWith("digest")) {
      val digest = authorization.split(" ")[1]
      if (StringUtils.isNotBlank(digest)) {
        return digest
      }
    }
    return null
  }

}
