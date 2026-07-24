package org.migor.feedless.http.mapper

import org.migor.feedless.document.Document
import org.migor.feedless.document.DocumentCreate
import org.migor.feedless.document.DocumentUpdate
import org.migor.feedless.document.enrichedTags
import org.migor.feedless.http.api.model.RecordCreate
import org.migor.feedless.http.api.model.RecordUpdate
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.util.toOffsetDateTime
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.Base64
import org.migor.feedless.http.api.model.Record as HttpRecord

@Component
class HttpRecordMapper {

  fun toHttp(document: Document): HttpRecord =
    HttpRecord(
      id = document.id.uuid,
      url = document.url,
      title = document.title,
      text = document.text,
      html = getHtml(document),
      tags = document.enrichedTags(),
      imageUrl = document.imageUrl,
      createdAt = document.createdAt.toOffsetDateTime(),
      publishedAt = document.publishedAt.toOffsetDateTime(),
      updatedAt = document.updatedAt.toOffsetDateTime(),
      startingAt = document.startingAt?.toOffsetDateTime(),
      rawBase64 = getRawBase64(document),
      rawMimeType = getRawMimeType(document),
    )

  fun toDomainCreate(body: RecordCreate): DocumentCreate =
    DocumentCreate(
      title = body.title,
      url = body.url,
      // DocumentCreate still carries epoch millis internally; the wire format is RFC3339.
      publishedAt = body.publishedAt.toInstant().toEpochMilli(),
      repositoryId = RepositoryId(body.repositoryId.toString()),
      text = body.text,
      tags = body.tags,
      rawBase64 = body.rawBase64,
      rawMimeType = body.rawMimeType,
    )

  fun toDomainUpdate(body: RecordUpdate): DocumentUpdate =
    DocumentUpdate(
      title = body.title,
      text = body.text,
      url = body.url,
      tags = body.tags,
    )

  private fun getHtml(document: Document): String? {
    return if (document.html.isNullOrBlank() && isHtml(document.rawMimeType)) {
      document.raw?.toString(StandardCharsets.UTF_8)
    } else {
      document.html
    }
  }

  private fun getRawBase64(document: Document): String? {
    return if (document.html.isNullOrBlank() && isHtml(document.rawMimeType)) {
      null
    } else {
      document.raw?.let { Base64.getEncoder().encodeToString(it) }
    }
  }

  private fun getRawMimeType(document: Document): String? {
    return if (document.html.isNullOrBlank() && isHtml(document.rawMimeType)) {
      null
    } else {
      document.rawMimeType
    }
  }

  private fun isHtml(rawMimeType: String?): Boolean =
    rawMimeType?.lowercase()?.startsWith("text/html") == true
}
