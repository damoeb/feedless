package org.migor.rich.rss.database.enums

data class NamespacedTag(val ns: TagNamespace, val tag: String)

enum class TagNamespace {
  CONTENT,
  INHERITED,
  USER,
  SUBSCRIPTION,
}
