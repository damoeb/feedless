package org.migor.feedless.pipeline.plugins

import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.parser.Tag
import org.migor.feedless.AppProfiles
import org.migor.feedless.attachment.AttachmentEntity
import org.migor.feedless.attachment.createAttachmentUrl
import org.migor.feedless.common.HttpService
import org.migor.feedless.common.PropertyService
import org.migor.feedless.document.DocumentEntity
import org.migor.feedless.generated.types.FeedlessPlugins
import org.migor.feedless.generated.types.PluginExecutionParamsInput
import org.migor.feedless.pipeline.MapEntityPlugin
import org.migor.feedless.plan.PlanConstraintsService
import org.migor.feedless.repository.RepositoryEntity
import org.migor.feedless.util.HtmlUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
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

  @Autowired
  lateinit var planConstraintsService: PlanConstraintsService

  override fun id(): String = FeedlessPlugins.org_feedless_privacy.name

  //  override fun description(): String = "Replaces links to images by base64 inlined images for enhanced privacy and longevity"
  override fun name(): String = "Privacy & Robustness"
  override fun listed() = true

  override fun mapEntity(
    corrId: String,
    document: DocumentEntity,
    repository: RepositoryEntity,
    params: PluginExecutionParamsInput
  ) {
    log.info("[$corrId] mapEntity ${document.url}")
    val response = httpService.httpGetCaching(corrId, document.url, 200)
    if (document.url != response.url) {
      log.info("[$corrId] Unwind url shortened urls ${document.url} -> ${response.url}")
      document.url = response.url
    }

//    if (planConstraintsService.can(FeatureName.itemsInlineImages)) {
    document.contentHtml?.let {
      val (html, attachments) = inlineImages(corrId, document.id, HtmlUtil.parseHtml(it, document.url))
      document.contentHtml = html
      document.attachments = attachments.toMutableList()
    } ?: log.info("[$corrId] no html content present ${document.id}")
//    }
  }

  fun inlineImages(corrId: String, documentId: UUID, document: Document): Pair<String, List<AttachmentEntity>> {
    val images = document.body().select("img[src]")
      .filter { imageElement -> imageElement.attr("src").startsWith("http") }
      .filterIndexed { index, _ -> index < 30 }

    log.info("[$corrId] inline ${images.size} images")
    val encoder = Base64.getEncoder()
    val attachments: List<AttachmentEntity> = images
      .mapNotNull { imageElement ->
        run {
          try {
            val response = httpService.httpGet(corrId, imageElement.attr("src"), 200)
            val imageFormat = response.contentType
            if (imageFormat.startsWith("image")) {
              val inlineImage = "data:${imageFormat};base64, " + encoder.encodeToString(
                resizeImage(response.responseBody)
              )

              imageElement.attr("src", inlineImage)
              val pElement = Element(Tag.valueOf("p"), "")
              imageElement.replaceWith(pElement)

              val attachment = AttachmentEntity()
              attachment.contentType = imageFormat
              attachment.data = response.responseBody
              attachment.hasData = false
              attachment.documentId = documentId

              val linkElement = Element(Tag.valueOf("a"), "")
              linkElement.attr("href", createAttachmentUrl(propertyService, attachment.id))
              linkElement.attr("target", "_blank")
              linkElement.appendText("Link to Image")

              pElement.appendChild(imageElement)
              pElement.appendChild(linkElement)

              attachment
            } else {
              null
            }
          } catch (t: Throwable) {
            log.warn("[${corrId}] ${t.message}")
            null
          }
        }
      }
    return Pair(document.body().html(), attachments)
  }

  @Throws(IOException::class)
  private fun resizeImage(rawData: ByteArray): ByteArray {
    val bais = ByteArrayInputStream(rawData)
    val image = ImageIO.read(bais)
    val maxWidth = 400
    val maxHeight = 400
    val height = image.height
    val width = image.width
    val ratio = (maxWidth / width.toDouble()).coerceAtMost(maxHeight / height.toDouble())
    val targetWidth = (width * ratio).toInt()
    val targetHeight = (height * ratio).toInt()
    val scaledImage = BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB)
    scaledImage.graphics.drawImage(image.getScaledInstance(targetWidth, targetHeight, Image.SCALE_DEFAULT), 0, 0, null)
    val baot = ByteArrayOutputStream()
    ImageIO.write(scaledImage, "webp", baot)
    return baot.toByteArray()
  }
}

