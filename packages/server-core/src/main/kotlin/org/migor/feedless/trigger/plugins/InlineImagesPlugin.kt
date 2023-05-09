package org.migor.feedless.trigger.plugins

import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.parser.Tag
import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.models.WebDocumentEntity
import org.migor.feedless.data.jpa.repositories.WebDocumentDAO
import org.migor.feedless.service.HttpService
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
class InlineImagesPlugin: WebDocumentPlugin {

  private val log = LoggerFactory.getLogger(InlineImagesPlugin::class.simpleName)

  @Autowired
  lateinit var webDocumentDAO: WebDocumentDAO

  @Autowired
  lateinit var httpService: HttpService

  override fun id(): String = "inlineImages"

  override fun executionPriority(): Int = 40

  override fun processWebDocument(corrId: String, webDocument: WebDocumentEntity) {
    webDocument.contentHtml()?.let {
      webDocumentDAO.saveContentRaw(webDocument.id,
        inlineImages(corrId, HtmlUtil.parseHtml(it, webDocument.url)),
        Date()
      )
    } ?: log.info("[$corrId] invalid mime ${webDocument.contentRawMime} ${webDocument.id}")
  }

  fun inlineImages(corrId: String, document: Document): String {
    val encoder = Base64.getEncoder()
    document.body().select("img[src]")
      .filter { imageElement -> imageElement.attr("src").startsWith("http") }
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
    ImageIO.write(scaledImage, "png", baot)
    return baot.toByteArray()
  }
}
