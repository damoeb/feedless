package org.migor.rss.rich.model

enum class ContentLevelPolicy {
  FIRST_DEGREE_CONTENT,
  SECOND_DEGREE_CONTENT, // lists the provided links as entries in a feed
  ALL
}
