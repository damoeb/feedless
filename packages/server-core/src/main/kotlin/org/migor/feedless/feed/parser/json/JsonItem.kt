package org.migor.feedless.feed.parser.json

import com.google.gson.annotations.SerializedName
import org.migor.feedless.generated.types.GeoPoint
import java.io.Serializable
import java.util.*

open class JsonItem : Serializable {

  companion object {
    const val ID = "id"
    const val TITLE = "title"
    const val URL = "url"
    const val STARTING_AT = "date_starting"
    const val LAT_LNG = "latlng"
    const val PUBLISHED_AT = "date_published"
  }

  @SerializedName(ID)
  lateinit var id: String

  @SerializedName(TITLE)
  lateinit var title: String

  @SerializedName(URL)
  lateinit var url: String

  @SerializedName("tags")
  var tags: List<String>? = null

  @SerializedName("content_text")
  var contentText: String? = null

  @SerializedName("content_raw")
  var contentRawBase64: String? = null

  @SerializedName("content_raw_mime")
  var contentRawMime: String? = null

  @SerializedName("content_html")
  var contentHtml: String? = null

  @SerializedName("summary")
  var summary: String? = null

  @SerializedName("image")
  var imageUrl: String? = null

  @SerializedName("banner_image")
  var bannerImage: String? = null

  @SerializedName("language")
  var language: String? = null

  @SerializedName("authors")
  var authors: List<JsonAuthor>? = null

  @SerializedName("attachments")
  var attachments: List<JsonAttachment> = emptyList()

  @SerializedName(PUBLISHED_AT)
  lateinit var publishedAt: Date

  @SerializedName("date_modified")
  var modifiedAt: Date? = null

  @SerializedName(STARTING_AT)
  var startingAt: Date? = null

  @SerializedName(LAT_LNG)
  var latLng: JsonPoint? = null
}
