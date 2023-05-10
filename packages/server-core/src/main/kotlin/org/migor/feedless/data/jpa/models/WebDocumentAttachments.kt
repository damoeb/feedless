package org.migor.feedless.data.jpa.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties
data class WebDocumentAttachments(val thumbnails: List<MediaThumbnail>, val media: List<MediaItem>)

@JsonIgnoreProperties
data class MediaItem(
  var url: String,
  var format: String? = null,
//  var size: Long? = null,
  var duration: Long? = null
)
@JsonIgnoreProperties
data class MediaThumbnail(val url: String)
