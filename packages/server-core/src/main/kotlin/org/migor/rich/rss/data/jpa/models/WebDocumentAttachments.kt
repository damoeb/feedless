package org.migor.rich.rss.data.jpa.models

data class WebDocumentAttachments(val thumbnails: List<MediaThumbnail>, val media: List<MediaItem>)

data class MediaItem(
  var url: String,
  var format: String? = null,
//  var size: Long? = null,
  var duration: Long? = null
)
data class MediaThumbnail(val url: String)
