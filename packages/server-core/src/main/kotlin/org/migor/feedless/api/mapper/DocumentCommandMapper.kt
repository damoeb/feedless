package org.migor.feedless.api.mapper

import org.migor.feedless.document.DocumentCreate
import org.migor.feedless.document.DocumentId
import org.migor.feedless.document.DocumentUpdate
import org.migor.feedless.generated.types.CreateRecordInput
import org.migor.feedless.generated.types.RecordUpdateInput
import org.migor.feedless.repository.RepositoryId

fun CreateRecordInput.toDomain(): DocumentCreate {
  return DocumentCreate(
    title = title,
    url = url,
    publishedAt = publishedAt,
    repositoryId = RepositoryId(repositoryId.id),
    text = text,
    tags = tags,
    rawBase64 = rawBase64,
    rawMimeType = rawMimeType,
    id = id?.let { DocumentId(it) },
    parentId = parent?.let { DocumentId(it.id) },
  )
}

fun RecordUpdateInput.toDomain(): DocumentUpdate {
  return DocumentUpdate(
    title = title?.set,
    text = text?.set,
    url = url?.set,
    tags = tags?.set,
    rawBase64 = rawBase64?.set,
    rawMimeType = rawMimeType?.set,
  )
}
