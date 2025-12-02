package org.migor.feedless.pipeline.plugins

import jakarta.annotation.PostConstruct
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.attachment.Attachment
import org.migor.feedless.attachment.AttachmentId
import org.migor.feedless.common.HttpResponse
import org.migor.feedless.common.HttpService
import org.migor.feedless.common.PropertyService
import org.migor.feedless.document.Document
import org.migor.feedless.document.DocumentId
import org.migor.feedless.generated.types.FeedlessPlugins
import org.migor.feedless.pipeline.MapEntityPlugin
import org.migor.feedless.repository.Repository
import org.migor.feedless.scrape.LogCollector
import org.migor.feedless.user.corrId
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
import kotlin.coroutines.coroutineContext
import org.jsoup.nodes.Document as JsoupDocument
import org.jsoup.nodes.Element as JsoupElement
import org.jsoup.parser.Tag as JsoupTag

@Service
@Profile("${AppProfiles.scrape} & ${AppLayer.service}")
class PrivacyPlugin : MapEntityPlugin<Unit> {

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

  override fun name(): String = "Privacy & Robustness"
  override fun listed() = true

  override suspend fun mapEntity(
    document: Document,
    repository: Repository,
    params: Unit,
    logCollector: LogCollector
  ): Document {
    log.debug("mapEntity ${document.url}")
    val response = httpService.httpGetCaching(document.url, 200)
    val url = if (document.url != response.url) {
      log.debug("Unwind url shortened urls ${document.url} -> ${response.url}")
      response.url
    } else {
      document.url
    }

//    if (planConstraintsService.can(FeatureName.itemsInlineImages)) {
    val updatedDocument = document.html?.let {
      val (html, attachments) = extractAttachments(document.id, HtmlUtil.parseHtml(it, url))
      document.copy(
        html = html,
        attachments = attachments.toMutableList()
      )
    } ?: run {
      log.debug("no html content present ${document.id}")
      document
    }
    return updatedDocument.copy(url = url)
//    }
  }

  override suspend fun mapEntity(
    document: Document,
    repository: Repository,
    paramsJson: String?,
    logCollector: LogCollector
  ): Document {
    return mapEntity(document, repository, null, logCollector)
  }

  override suspend fun fromJson(jsonParams: String?) {
  }

  suspend fun extractAttachments(
    documentId: DocumentId,
    document: JsoupDocument
  ): Pair<String, List<Attachment>> {
    val attachments = mutableListOf<Attachment>()
      .plus(
        filterBlacklisted(document.links())
          .filterIndexed { index, _ -> index < 10 }
          .mapNotNull { handleLinkedData(it, documentId) })
//      .plus(document.images()
//        .filterIndexed { index, _ -> index < 10 }
//        .mapNotNull {
//          handleImage(it, documentId)
//        })

    return Pair(document.body().html(), attachments)
  }

  private fun filterBlacklisted(links: List<JsoupElement>): List<JsoupElement> {
    return links.filter { link -> blacklistedDomains.none { link.attr("href").contains(it) } }
  }

  private suspend fun handleImage(
    imageElement: JsoupElement,
    documentId: DocumentId
  ): Attachment? {
    val corrId = coroutineContext.corrId()
    return try {
      val url = imageElement.attr("src")
      val response = fetch(url, arrayOf("image/jpeg", "image/png", "image/webp"))

      val encoder = Base64.getEncoder()
      val (isResized, resizedImage, origFormat) = resizeImage(response.responseBody)
      val inlineImage = "data:${response.contentType};base64, " + encoder.encodeToString(resizedImage)
      imageElement.attr("src", inlineImage)
      imageElement.removeAttr("width")
      imageElement.removeAttr("height")
      imageElement.removeAttr("srcSet")

      if (isResized) {
        val wrapper = JsoupElement(JsoupTag.valueOf("div"), "")
        imageElement.replaceWith(wrapper)

        val imageWrapper = JsoupElement(JsoupTag.valueOf("div"), "")
        imageWrapper.appendChild(imageElement)
        wrapper.appendChild(imageWrapper)

        val attachment = toAttachment(response, documentId)

        val attachmentLinkElement = JsoupElement(JsoupTag.valueOf("a"), "")
        attachmentLinkElement.attr("href", createAttachmentUrl(propertyService, attachment.id))
        attachmentLinkElement.attr("target", "_blank")
        attachmentLinkElement.appendText("Full Image $origFormat")

        val textWrapper = JsoupElement(JsoupTag.valueOf("div"), "")
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

  private suspend fun fetch(url: String, contentTypes: Array<String>): HttpResponse {
    val response = httpService.httpGet(url, 200, null)

    if (contentTypes.none { response.contentType.lowercase().startsWith(it) }) {
      throw IllegalArgumentException("Ignoring ContentType ${response.contentType}")
    }
    return response
  }

  private fun toAttachment(
    response: HttpResponse,
    documentId: DocumentId
  ): Attachment {
    return Attachment(
      mimeType = response.contentType,
      originalUrl = response.url,
      data = response.responseBody,
      hasData = response.responseBody.isNotEmpty(),
      documentId = documentId
    )
  }

  private suspend fun handleLinkedData(
    linkElement: JsoupElement,
    documentId: DocumentId
  ): Attachment? {
    val corrId = coroutineContext.corrId()
    return try {
      val response = fetch(linkElement.attr("href"), arrayOf("application/pdf"))
      val attachment = toAttachment(response, documentId)
      linkElement.attr("href", createAttachmentUrl(propertyService, attachment.id))
      attachment
    } catch (t: Throwable) {
      log.debug("[${corrId}] ${t.message}")
      null
    }
  }

  @Throws(IOException::class)
  private fun resizeImage(rawData: ByteArray): Triple<Boolean, ByteArray, String?> {
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
      log.debug("resizing from ${width}x$height to ${targetWidth}x$targetHeight")
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

fun JsoupDocument.images(): List<JsoupElement> {
  return body().select("img[src]")
    .filter { imageElement -> imageElement.attr("src").startsWith("http") }
}

private fun JsoupDocument.links(): List<JsoupElement> {
  return body().select("a[href]")
    .filter { imageElement -> imageElement.attr("href").startsWith("http") }
}


fun createAttachmentUrl(propertyService: PropertyService, id: AttachmentId): String =
  "${propertyService.apiGatewayUrl}/attachment/${id.uuid}"
