package org.migor.feedless.pipeline.plugins

import org.apache.commons.lang3.StringUtils
import org.migor.feedless.document.Document
import org.migor.feedless.feed.parser.json.JsonAttachment
import org.migor.feedless.feed.parser.json.JsonItem
import org.migor.feedless.feed.parser.json.JsonPoint
import org.migor.feedless.repository.Repository
import java.util.*

fun Document.asJsonItem(repository: Repository? = null): JsonItem {
  val item = JsonItem()
  item.id = id.toString()
  latLon?.let {
    val point = JsonPoint()
    point.x = it.x
    point.y = it.y
    item.latLng = point
  }
  item.title = StringUtils.trimToEmpty(title)
  item.attachments = attachments.map {
    JsonAttachment(
      url = StringUtils.trimToEmpty(it.remoteDataUrl),
      type = it.mimeType,
      length = it.size,
      duration = it.duration
    )
  }
  item.url = url
  item.repositoryId = repositoryId
  item.repositoryName = repository?.title
  item.text = StringUtils.trimToEmpty(text)
  item.rawBase64 = raw?.let { Base64.getEncoder().encodeToString(raw) }
  item.rawMimeType = rawMimeType
  item.html = html
  item.publishedAt = publishedAt
  item.modifiedAt = updatedAt
  item.tags = (tags?.asList() ?: emptyList())
  item.startingAt = startingAt
  item.imageUrl = imageUrl
  return item

}
