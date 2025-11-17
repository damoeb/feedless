package org.migor.feedless.api

import org.apache.commons.lang3.StringUtils
import org.migor.feedless.common.PropertyService
import org.migor.feedless.document.Document
import org.migor.feedless.generated.types.Attachment
import org.migor.feedless.generated.types.GeoPoint
import org.migor.feedless.generated.types.Record
import org.migor.feedless.pipeline.plugins.createAttachmentUrl
import org.migor.feedless.repository.addListenableTag
import org.migor.feedless.repository.classifyDuration
import org.migor.feedless.util.toMillis
import java.nio.charset.StandardCharsets
import java.util.*

fun Document.toDto(propertyService: PropertyService): Record {
  val htmlParam: String?
  var rawBase64Param: String? = null
  var rawMimeTypeParam: String? = null
  if (StringUtils.isBlank(html) && isHtml(rawMimeType)) {
    htmlParam = raw?.toString(StandardCharsets.UTF_8)
  } else {
    htmlParam = html
    rawBase64Param = raw?.let { Base64.getEncoder().encodeToString(raw) }
    rawMimeTypeParam = rawMimeType
  }

  return Record(
    id = id.toString(),
    imageUrl = imageUrl,
    url = url,
    html = htmlParam,
    rawBase64 = rawBase64Param,
    rawMimeType = rawMimeTypeParam,
    title = title,
    text = text,
    createdAt = createdAt.toMillis(),
    updatedAt = updatedAt.toMillis(),
    latLng = latLon?.let {
      GeoPoint(
        lat = it.x,
        lng = it.y,
      )
    },
    tags = (tags?.asList() ?: emptyList()).plus(
      addListenableTag(
        attachments.filter { it.mimeType.startsWith("audio/") && it.duration != null }
          .map { classifyDuration(it.duration!!) }.distinct()
      )
    ),

    attachments = (attachments.map {
      Attachment(
        id = it.id.toString(),
        url = it.remoteDataUrl ?: createAttachmentUrl(propertyService, it.id.uuid),
        type = it.mimeType,
        duration = it.duration,
        size = it.size,
      )
    }),
    publishedAt = publishedAt.toMillis(),
    startingAt = startingAt?.toMillis(),
  )
}

fun createDocumentUrl(propertyService: PropertyService, id: UUID): String =
  "${propertyService.apiGatewayUrl}/article/${id}"
