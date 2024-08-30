package org.migor.feedless.pipeline.plugins

import jakarta.annotation.PostConstruct
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.parser.Tag
import org.migor.feedless.AppProfiles
import org.migor.feedless.attachment.AttachmentEntity
import org.migor.feedless.attachment.createAttachmentUrl
import org.migor.feedless.common.HttpResponse
import org.migor.feedless.common.HttpService
import org.migor.feedless.common.PropertyService
import org.migor.feedless.document.DocumentEntity
import org.migor.feedless.generated.types.FeedlessPlugins
import org.migor.feedless.generated.types.PluginExecutionParamsInput
import org.migor.feedless.pipeline.MapEntityPlugin
import org.migor.feedless.repository.RepositoryEntity
import org.migor.feedless.util.HtmlUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*
import javax.imageio.ImageIO

@Service
@Profile(AppProfiles.database)
class PrivacyPlugin : MapEntityPlugin {

  private val log = LoggerFactory.getLogger(PrivacyPlugin::class.simpleName)

  @Autowired
  lateinit var httpService: HttpService

  @Autowired
  lateinit var propertyService: PropertyService

  @Value("\${APP_BLACKLISTED_DOMAINS:}")
  lateinit var blacklistedDomainsStr: String

  private val blacklistedDomains = mutableSetOf<String>("doubleclick.net")

//  @Autowired
//  private lateinit var planConstraintsService: PlanConstraintsService

  override fun id(): String = FeedlessPlugins.org_feedless_privacy.name

  //  override fun description(): String = "Replaces links to images by base64 inlined images for enhanced privacy and longevity"
  override fun name(): String = "Privacy & Robustness"
  override fun listed() = true

  override suspend fun mapEntity(
    corrId: String,
    document: DocumentEntity,
    repository: RepositoryEntity,
    params: PluginExecutionParamsInput
  ): DocumentEntity {
    log.debug("[$corrId] mapEntity ${document.url}")
    val response = httpService.httpGetCaching(corrId, document.url, 200)
    if (document.url != response.url) {
      log.debug("[$corrId] Unwind url shortened urls ${document.url} -> ${response.url}")
      document.url = response.url
    }

//    if (planConstraintsService.can(FeatureName.itemsInlineImages)) {
    document.contentHtml?.let {
      val (html, attachments) = extractAttachments(corrId, document.id, HtmlUtil.parseHtml(it, document.url))
      document.contentHtml = html
      document.attachments = attachments.toMutableList()
    } ?: log.debug("[$corrId] no html content present ${document.id}")
//    }

    return document
  }

  suspend fun extractAttachments(
    corrId: String,
    documentId: UUID,
    document: Document
  ): Pair<String, List<AttachmentEntity>> {
    val attachments = mutableListOf<AttachmentEntity>()
      .plus(
        filterBlacklisted(document.links())
          .filterIndexed { index, _ -> index < 10 }
          .mapNotNull { handleLinkedData(corrId, it, documentId) })
      .plus(document.images()
        .filterIndexed { index, _ -> index < 10 }
        .mapNotNull {
          handleImage(corrId, it, documentId)
        })

    return Pair(document.body().html(), attachments)
  }

  private fun filterBlacklisted(links: List<Element>): List<Element> {
    return links.filter { link -> blacklistedDomains.none { link.attr("href").contains(it) } }
  }

  private suspend fun handleImage(
    corrId: String,
    imageElement: Element,
    documentId: UUID
  ): AttachmentEntity? {
    return try {
      val url = imageElement.attr("src")
      val response = fetch(corrId, url, arrayOf("image/jpeg", "image/png", "image/webp"))

      val encoder = Base64.getEncoder()
      val (isResized, resizeImage, origFormat) = resizeImage(corrId, response.responseBody)
      val inlineImage = "data:${response.contentType};base64, " + encoder.encodeToString(resizeImage)
      imageElement.attr("src", inlineImage)
      imageElement.removeAttr("width")
      imageElement.removeAttr("height")
      imageElement.removeAttr("srcSet")

      if (isResized) {
        val wrapper = Element(Tag.valueOf("div"), "")
        imageElement.replaceWith(wrapper)

        val imageWrapper = Element(Tag.valueOf("div"), "")
        imageWrapper.appendChild(imageElement)
        wrapper.appendChild(imageWrapper)

        val attachment = toAttachment(response, documentId)

        val attachmentLinkElement = Element(Tag.valueOf("a"), "")
        attachmentLinkElement.attr("href", createAttachmentUrl(propertyService, attachment.id))
        attachmentLinkElement.attr("target", "_blank")
        attachmentLinkElement.appendText("Full Image $origFormat")

        val textWrapper = Element(Tag.valueOf("div"), "")
        textWrapper.appendChild(attachmentLinkElement)

        wrapper.appendChild(textWrapper)

        attachment
      } else {
        null
      }
    } catch (e: IllegalArgumentException) {
      log.warn("[${corrId}] ${e.message}", e)
      null
    }
  }

  private suspend fun fetch(corrId: String, url: String, contentTypes: Array<String>): HttpResponse {
    val response = httpService.httpGet(corrId, url, 200, null)

    if (contentTypes.none { response.contentType.lowercase().startsWith(it) }) {
      throw IllegalArgumentException("Ignoring ContentType ${response.contentType}")
    }
    return response
  }

  private fun toAttachment(
    response: HttpResponse,
    documentId: UUID
  ): AttachmentEntity {
    val attachment = AttachmentEntity()
    attachment.contentType = response.contentType
    attachment.originalUrl = response.url
    attachment.data = response.responseBody
    attachment.documentId = documentId
    return attachment
  }

  private suspend fun handleLinkedData(
    corrId: String,
    linkElement: Element,
    documentId: UUID
  ): AttachmentEntity? {
    return try {
      val response = fetch(corrId, linkElement.attr("href"), arrayOf("application/pdf"))
      val attachment = toAttachment(response, documentId)
      linkElement.attr("href", createAttachmentUrl(propertyService, attachment.id))
      attachment
    } catch (t: Throwable) {
      log.debug("[${corrId}] ${t.message}")
      null
    }
  }

  @Throws(IOException::class)
  private fun resizeImage(corrId: String, rawData: ByteArray): Triple<Boolean, ByteArray, String?> {
    val bais = ByteArrayInputStream(rawData)
    val image = ImageIO.read(bais)
    val maxWidth = 600
    val maxHeight = 300
    val height = image.height
    val width = image.width

    val shouldResize = width > maxWidth || height > maxHeight
    return if (shouldResize) {
      val ratio = (maxWidth / width.toDouble()).coerceAtMost(maxHeight / height.toDouble())
      val targetWidth = (width * ratio).toInt()
      val targetHeight = (height * ratio).toInt()
      log.info("[$corrId] resizing from ${width}x$height to ${targetWidth}x$targetHeight")
      val scaledImage = BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB)
      scaledImage.graphics.drawImage(
        image.getScaledInstance(targetWidth, targetHeight, Image.SCALE_DEFAULT),
        0,
        0,
        null
      )
      val baot = ByteArrayOutputStream()
      ImageIO.write(scaledImage, "webp", baot)
      Triple(true, baot.toByteArray(), "$targetWidth x $targetHeight")
    } else {
      Triple(false, rawData, null)
    }
  }

  @PostConstruct
  fun postConstruct() {
    blacklistedDomains.addAll(blacklistedDomainsStr.split(" ").filterNot { it.trim().isBlank() })
  }
}

fun Document.images(): List<Element> {
  return body().select("img[src]")
    .filter { imageElement -> imageElement.attr("src").startsWith("http") }
}

private fun Document.links(): List<Element> {
  return body().select("a[href]")
    .filter { imageElement -> imageElement.attr("href").startsWith("http") }
}

