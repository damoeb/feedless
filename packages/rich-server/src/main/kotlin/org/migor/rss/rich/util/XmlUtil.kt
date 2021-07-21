package org.migor.rss.rich.util

object XmlUtil {
  fun explicitCloseTags(xml: String): String {
//    val regex = "<([a-z0-9-_:]+)[ ]?([^>]*)/>".toRegex(RegexOption.IGNORE_CASE)
//    return regex.replace(xml) {
//      match: MatchResult -> val tag = match.groups[1]!!.value
//      "<$tag ${match.groups[2]?.value}></${tag}>"
//    }
    return xml
  }
}
