package org.migor.feedless.pipeline.plugins

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import org.apache.commons.lang3.StringUtils
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.document.Document
import org.migor.feedless.feed.parser.json.JsonAttachment
import org.migor.feedless.feed.parser.json.JsonItem
import org.migor.feedless.feed.parser.json.JsonPoint
import org.migor.feedless.generated.types.FeedlessPlugins
import org.migor.feedless.pipeline.MapEntityPlugin
import org.migor.feedless.repository.Repository
import org.migor.feedless.scrape.LogCollector
import org.migor.feedless.user.corrId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*
import kotlin.coroutines.coroutineContext

data class ConditionalTag(
    @SerializedName("tag") val tag: String,
    @SerializedName("filter") val filter: CompositeFieldFilterParams,
)

data class CompositeFieldFilterParams(
    @SerializedName("index") val index: NumericalFilterParams? = null,
    @SerializedName("title") val title: StringFilterParams? = null,
    @SerializedName("content") val content: StringFilterParams? = null,
    @SerializedName("link") val link: StringFilterParams? = null,
)

typealias ConditionalTagPluginParams = List<ConditionalTag>

@Service
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.scrape} & ${AppLayer.service}")
class ConditionalTagPlugin : MapEntityPlugin<ConditionalTagPluginParams> {

    private val log = LoggerFactory.getLogger(ConditionalTagPlugin::class.simpleName)

    @Autowired
    private lateinit var filterPlugin: CompositeFilterPlugin


    override fun id(): String = FeedlessPlugins.org_feedless_conditional_tag.name
    override fun name(): String = "Conditional Tags"

    override fun listed() = true
    override suspend fun mapEntity(
        document: Document,
        repository: Repository,
        params: ConditionalTagPluginParams,
        logCollector: LogCollector
    ): Document {
        val corrId = coroutineContext.corrId()
        log.debug("[$corrId] mapEntity ${document.url}")
        val newTags = params.filter {
            filterPlugin.matches(document.asJsonItem(), it.filter, 0)
        }.map { it.tag }.toMutableSet()

        return if (newTags.isNotEmpty()) {
            document.tags?.let { newTags.addAll(it) }
            document.copy(
                tags = newTags.distinct().sorted().toTypedArray()
            )
        } else {
            document
        }
    }

    override suspend fun mapEntity(
        document: Document,
        repository: Repository,
        paramsJson: String?,
        logCollector: LogCollector
    ): Document {
        return mapEntity(document, repository, fromJson(paramsJson), logCollector)
    }

    override suspend fun fromJson(jsonParams: String?): ConditionalTagPluginParams {
        return Gson().fromJson(jsonParams, ConditionalTagPluginParams::class.java)
    }
}

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
