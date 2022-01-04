package org.migor.rss.rich.database.model

data class NamespacedTag(val ns: TagNamespace, val tag: String)

enum class TagNamespace {
  CONTENT,
  INHERITED,
  USER,
  SUBSCRIPTION,
}
