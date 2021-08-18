package org.migor.rss.rich.database.model


data class NamespacedTag(val namespace: TagNamespace, val tag: String)

enum class TagNamespace {
  CONTENT,
  NONE,
  USER,
  SUBSCRIPTION
}
