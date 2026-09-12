package org.migor.feedless.feed.parser.json

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Contextual
import org.migor.feedless.repository.RepositoryId
import java.io.Serializable
import java.time.LocalDateTime

@kotlinx.serialization.Serializable
open class JsonItem : Serializable {

  companion object {
    const val ID = "id"
    const val TITLE = "title"
    const val URL = "url"
    const val STARTING_AT = "date_starting"
    const val ENDING_AT = "date_ending"
    const val LAT_LNG = "latlng"
    const val PUBLISHED_AT = "date_published"
  }

  @Transient
  @Contextual
  var repositoryId: RepositoryId? = null

  @Transient
  var repositoryName: String? = null

  @SerializedName(ID)
  lateinit var id: String

  @SerializedName(TITLE)
  lateinit var title: String

  @SerializedName(URL)
  lateinit var url: String

  @SerializedName("tags")
  var tags: List<String>? = null

  @SerializedName("content_text")
  var text: String? = null

  @SerializedName("content_raw")
  var rawBase64: String? = null

  @SerializedName("content_raw_mime")
  var rawMimeType: String? = null

  @SerializedName("content_html")
  var html: String? = null

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

  @Contextual
  @SerializedName(PUBLISHED_AT)
  lateinit var publishedAt: LocalDateTime

  @Contextual
  @SerializedName("date_modified")
  var modifiedAt: LocalDateTime? = null

  @Contextual
  @SerializedName(STARTING_AT)
  var startingAt: LocalDateTime? = null

  @Contextual
  @SerializedName(ENDING_AT)
  var endingAt: LocalDateTime? = null

  @SerializedName(LAT_LNG)
  var latLng: JsonPoint? = null

}
