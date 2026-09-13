package org.migor.feedless.document

import org.migor.feedless.repository.RepositoryId

data class DocumentCreate(
  val title: String,
  val url: String,
  val publishedAt: Long,
  val repositoryId: RepositoryId,
  val text: String? = null,
  val tags: List<String>? = null,
  val rawBase64: String? = null,
  val rawMimeType: String? = null,
  val id: DocumentId? = null,
  val parentId: DocumentId? = null,
)
