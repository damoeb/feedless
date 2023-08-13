package org.migor.feedless.web

class ExtractedArticle(var originalUrl: String) {
  var url: String? = null
  var imageUrl: String? = null
  var title: String? = null
  var contentText: String? = null
  var content: String? = null
  var contentMime: String? = null
  var faviconUrl: String? = null
  var date: String? = null

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as ExtractedArticle

    if (originalUrl != other.originalUrl) return false
    if (url != other.url) return false
    if (imageUrl != other.imageUrl) return false
    if (title != other.title) return false
    if (contentText != other.contentText) return false
    if (!content.contentEquals(other.content)) return false
    if (faviconUrl != other.faviconUrl) return false
    return date == other.date
  }

  override fun hashCode(): Int {
    var result = originalUrl.hashCode()
    result = 31 * result + (url?.hashCode() ?: 0)
    result = 31 * result + (imageUrl?.hashCode() ?: 0)
    result = 31 * result + (title?.hashCode() ?: 0)
    result = 31 * result + (contentText?.hashCode() ?: 0)
    result = 31 * result + (content?.hashCode() ?: 0)
    result = 31 * result + (faviconUrl?.hashCode() ?: 0)
    result = 31 * result + (date?.hashCode() ?: 0)
    return result
  }


}
