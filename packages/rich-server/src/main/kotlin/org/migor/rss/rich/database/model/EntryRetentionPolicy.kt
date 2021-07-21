package org.migor.rss.rich.database.model

enum class EntryRetentionPolicy {
  ARCHIVE, // everything, forever
  MINIMAL // a week or max 100 entries
}
