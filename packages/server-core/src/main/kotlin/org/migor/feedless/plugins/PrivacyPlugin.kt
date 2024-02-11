package org.migor.feedless.plugins

import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.parser.Tag
import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.models.SourceSubscriptionEntity
import org.migor.feedless.data.jpa.models.WebDocumentEntity
import org.migor.feedless.generated.types.FeedlessPlugins
import org.migor.feedless.generated.types.PluginExecutionParamsInput
import org.migor.feedless.service.HttpService
import org.migor.feedless.service.PlanConstraintsService
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
  lateinit var planConstraintsService: PlanConstraintsService

  override fun id(): String = FeedlessPlugins.org_feedless_privacy.name

  //  override fun description(): String = "Replaces links to images by base64 inlined images for enhanced privacy and longevity"
  override fun name(): String = "Privacy & Robustness"
  override fun listed() = true

  override fun mapEntity(
    corrId: String,
    webDocument: WebDocumentEntity,
    subscription: SourceSubscriptionEntity,
    params: PluginExecutionParamsInput
  ) {
    log.info("[$corrId] mapEntity ${webDocument.url}")
    val response = httpService.httpGetCaching(corrId, webDocument.url, 200)
    if (webDocument.url != response.url) {
      log.info("[$corrId] Unwind url shortened urls ${webDocument.url} -> ${response.url}")
      webDocument.url = response.url
    }

//    if (planConstraintsService.can(FeatureName.itemsInlineImages)) {
    webDocument.contentHtml?.let {
      webDocument.contentHtml = inlineImages(corrId, HtmlUtil.parseHtml(it, webDocument.url))
    } ?: log.info("[$corrId] invalid mime ${webDocument.contentRawMime} ${webDocument.id}")
//    }
  }

  fun inlineImages(corrId: String, document: Document): String {
    val images = document.body().select("img[src]")
      .filter { imageElement -> imageElement.attr("src").startsWith("http") }
      .filterIndexed { index, _ -> index < 30 }

    log.info("[$corrId] inline ${images.size} images")
    val encoder = Base64.getEncoder()
    images
      .forEach { imageElement ->
        runCatching {
          val response = httpService.httpGet(corrId, imageElement.attr("src"), 200)
          val imageFormat = response.contentType
          if (imageFormat.startsWith("image")) {
            val inlineImage = "data:${imageFormat};base64, " + encoder.encodeToString(
              resizeImage(response.responseBody)
            )
            imageElement.attr("src", inlineImage)
            val pElement = Element(Tag.valueOf("p"), "")
            imageElement.replaceWith(pElement)

            val linkElement = Element(Tag.valueOf("a"), "")
            linkElement.attr("href", imageElement.attr("src"))
            linkElement.attr("target", "_blank")
            linkElement.appendText("Link to Image")

            pElement.appendChild(imageElement)
            pElement.appendChild(linkElement)
          }
        }.onFailure {
          log.warn("[${corrId}] ${it.message}")
        }
      }
    return document.body().html()
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
