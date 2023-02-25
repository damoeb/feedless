package org.migor.rich.rss.data.jpa.enums

enum class ArticleSource(val id: Int) {
  WEBSITE(0),
  FEED(1);

  companion object {
    fun findById(id: Int?): ArticleSource? {
      return values().find { bucketType -> bucketType.id == id }
    }

    fun findByName(type: String?): ArticleSource? {
      return values().find { bucketType -> bucketType.name == type?.lowercase() }
    }
  }
}
