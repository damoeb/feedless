package org.migor.rss.rich.util

object TagUtil {

  fun tag(prefix: TagPrefix, value: String): String {
    return "${prefix.prefix}:${value}"
  }
}
