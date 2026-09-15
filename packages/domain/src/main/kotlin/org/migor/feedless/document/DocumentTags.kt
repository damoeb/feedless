package org.migor.feedless.document

fun Document.enrichedTags(): List<String> {
  val baseTags = tags?.asList() ?: emptyList()
  val audioAttachments = attachments
    .filter { it.mimeType.startsWith("audio/") && it.duration != null }
    .map { classifyDuration(it.duration!!) }
    .distinct()
  return baseTags.plus(addListenableTag(audioAttachments))
}

fun addListenableTag(tags: List<String>): List<String> =
  if (tags.isEmpty()) tags else tags.plus("listenable")

fun classifyDuration(duration: Long): String =
  when (duration.div(60.0)) {
    in 0.0..1.0 -> "brief"
    in 1.0..5.0 -> "short"
    in 5.0..30.0 -> "medium"
    else -> "long"
  }
