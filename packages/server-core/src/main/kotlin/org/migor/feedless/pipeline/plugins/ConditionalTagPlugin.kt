package org.migor.feedless.pipeline.plugins

import org.apache.commons.lang3.StringUtils
import org.migor.feedless.AppProfiles
import org.migor.feedless.document.DocumentEntity
import org.migor.feedless.feed.parser.json.JsonAttachment
import org.migor.feedless.feed.parser.json.JsonItem
import org.migor.feedless.feed.parser.json.JsonPoint
import org.migor.feedless.generated.types.FeedlessPlugins
import org.migor.feedless.generated.types.PluginExecutionParamsInput
import org.migor.feedless.pipeline.MapEntityPlugin
import org.migor.feedless.repository.RepositoryEntity
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.util.*

@Service
@Profile(AppProfiles.scrape)
class ConditionalTagPlugin : MapEntityPlugin {

  private val log = LoggerFactory.getLogger(ConditionalTagPlugin::class.simpleName)

  @Autowired
  private lateinit var filterPlugin: CompositeFilterPlugin


  override fun id(): String = FeedlessPlugins.org_feedless_conditional_tag.name
  override fun name(): String = "Conditional Tags"

  override fun listed() = true
  override suspend fun mapEntity(
    corrId: String,
    document: DocumentEntity,
    repository: RepositoryEntity,
    params: PluginExecutionParamsInput
  ): DocumentEntity {
    log.debug("[$corrId] mapEntity ${document.url}")
    val newTags = params.org_feedless_conditional_tag!!.filter {
      filterPlugin.matches(document.asJsonItem(), it.filter, 0)
    }.map { it.tag }.toMutableSet()

    if (newTags.isNotEmpty()) {
      document.tags?.let { newTags.addAll(it) }
      document.tags = newTags.distinct().sorted().toTypedArray()
    }

    return document
  }
}

fun DocumentEntity.asJsonItem(): JsonItem {
  val item = JsonItem()
  item.id = id.toString()
  latLon?.let {
    val point = JsonPoint()
    point.x = it.x
    point.y = it.y
    item.latLng = point
  }
  item.title = StringUtils.trimToEmpty(contentTitle)
  item.attachments = attachments.map {
    JsonAttachment(
      url = StringUtils.trimToEmpty(it.remoteDataUrl),
      type = it.contentType,
      length = it.size,
      duration = it.duration
    )
  }
  item.url = url
  item.contentText = StringUtils.trimToEmpty(contentText)
  item.contentRawBase64 = contentRaw?.let { Base64.getEncoder().encodeToString(contentRaw) }
  item.contentRawMime = contentRawMime
  item.contentHtml = contentHtml
  item.publishedAt = publishedAt
  item.modifiedAt = updatedAt
  item.tags = (tags?.asList() ?: emptyList())
  item.startingAt = startingAt
  item.imageUrl = imageUrl
  return item

}
