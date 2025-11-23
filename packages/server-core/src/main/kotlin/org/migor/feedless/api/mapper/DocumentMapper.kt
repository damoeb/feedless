package org.migor.feedless.api.mapper

import org.apache.commons.lang3.StringUtils
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.migor.feedless.common.PropertyService
import org.migor.feedless.document.Document
import org.migor.feedless.generated.types.Attachment
import org.migor.feedless.generated.types.GeoPoint
import org.migor.feedless.generated.types.Record
import org.migor.feedless.pipeline.plugins.createAttachmentUrl
import org.migor.feedless.repository.addListenableTag
import org.migor.feedless.repository.classifyDuration
import java.nio.charset.StandardCharsets
import java.util.*

/**
 * MapStruct mapper for Document domain to DTO
 */
@Mapper(config = MapStructConfig::class)
abstract class DocumentMapper {

    @Mapping(target = "id", expression = "java(document.getId().toString())")
    @Mapping(target = "html", expression = "java(getHtml(document))")
    @Mapping(target = "rawBase64", expression = "java(getRawBase64(document))")
    @Mapping(target = "rawMimeType", expression = "java(getRawMimeType(document))")
    @Mapping(target = "createdAt", expression = "java(MapperUtil.toMillis(document.getCreatedAt()))")
    @Mapping(target = "updatedAt", expression = "java(MapperUtil.toMillis(document.getUpdatedAt()))")
    @Mapping(target = "latLng", expression = "java(getLatLng(document))")
    @Mapping(target = "tags", expression = "java(getTags(document))")
    @Mapping(target = "attachments", expression = "java(getAttachments(document, propertyService))")
    @Mapping(target = "publishedAt", expression = "java(MapperUtil.toMillis(document.getPublishedAt()))")
    @Mapping(target = "startingAt", expression = "java(MapperUtil.toMillis(document.getStartingAt()))")
    @Mapping(target = "imageUrl", source = "document.imageUrl")
    @Mapping(target = "url", source = "document.url")
    @Mapping(target = "title", source = "document.title")
    @Mapping(target = "text", source = "document.text")
    abstract fun toDto(document: Document, propertyService: PropertyService): Record

    protected fun getHtml(document: Document): String? {
        return if (StringUtils.isBlank(document.html) && isHtml(document.rawMimeType)) {
            document.raw?.toString(StandardCharsets.UTF_8)
        } else {
            document.html
        }
    }

    protected fun getRawBase64(document: Document): String? {
        return if (StringUtils.isBlank(document.html) && isHtml(document.rawMimeType)) {
            null
        } else {
            document.raw?.let { Base64.getEncoder().encodeToString(it) }
        }
    }

    protected fun getRawMimeType(document: Document): String? {
        return if (StringUtils.isBlank(document.html) && isHtml(document.rawMimeType)) {
            null
        } else {
            document.rawMimeType
        }
    }

    protected fun getLatLng(document: Document): GeoPoint? {
        return document.latLon?.let {
            GeoPoint(
                lat = it.x,
                lng = it.y,
            )
        }
    }

    protected fun getTags(document: Document): List<String> {
        val baseTags = document.tags?.asList() ?: emptyList()
        val audioAttachments = document.attachments
            .filter { it.mimeType.startsWith("audio/") && it.duration != null }
            .map { classifyDuration(it.duration!!) }
            .distinct()
        return baseTags.plus(addListenableTag(audioAttachments))
    }

    protected fun getAttachments(document: Document, propertyService: PropertyService): List<Attachment> {
        return document.attachments.map {
            Attachment(
                id = it.id.toString(),
                url = it.remoteDataUrl ?: createAttachmentUrl(propertyService, it.id),
                type = it.mimeType,
                duration = it.duration,
                size = it.size,
            )
        }
    }

    private fun isHtml(rawMimeType: String?): Boolean =
        rawMimeType?.lowercase()?.startsWith("text/html") == true

}

