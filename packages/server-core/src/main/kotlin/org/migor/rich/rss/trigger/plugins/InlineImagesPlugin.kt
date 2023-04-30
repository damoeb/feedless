package org.migor.rich.rss.trigger.plugins

import org.jsoup.nodes.Element
import org.jsoup.parser.Tag
import org.migor.rich.rss.AppProfiles
import org.migor.rich.rss.data.jpa.models.WebDocumentEntity
import org.migor.rich.rss.data.jpa.repositories.WebDocumentDAO
import org.migor.rich.rss.service.HttpService
import org.migor.rich.rss.util.HtmlUtil
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

  override fun executionPriority(): Int = 30

  override fun processWebDocument(corrId: String, webDocument: WebDocumentEntity) {
    if (webDocument.contentRawMime!!.startsWith("text/html")) {
      webDocumentDAO.saveContentRaw(webDocument.id,
        inlineImages(corrId, webDocument),
        Date()
      )
    } else {
      log.info("[$corrId] invalid mime ${webDocument.id}")
    }
  }

  private fun inlineImages(corrId: String, webDocument: WebDocumentEntity): String {
    val document = HtmlUtil.parseHtml(webDocument.contentRaw!!, webDocument.url)
    val encoder = Base64.getEncoder()
    document.body().select("img[src]")
      .filter { imageElement -> imageElement.attr("src").startsWith("http") }
      .forEach { imageElement ->
        runCatching {
          val response = httpService.httpGet(corrId, imageElement.attr("src"), 200)
          val imageFormat = "png"
          imageElement.attr(
            "src",
            "data:image/${imageFormat};base64, " + encoder.encodeToString(
              resizeImage(
                response.responseBody,
                imageFormat
              )
            )
          )
          val pElement = Element(Tag.valueOf("p"), "")
          imageElement.replaceWith(pElement)

          val linkElement = Element(Tag.valueOf("a"), "")
          linkElement.attr("href", imageElement.attr("src"))
          linkElement.attr("target", "_blank")
          linkElement.appendText("Link to Image")

          pElement.appendChild(imageElement)
          pElement.appendChild(linkElement)
        }.onFailure {
          log.warn("[${corrId}] ${it.message}")
        }
      }
    return document.body().html()
  }

  @Throws(IOException::class)
  private fun resizeImage(rawData: ByteArray, imageFormat: String): ByteArray {
    val bais = ByteArrayInputStream(rawData)
    val image = ImageIO.read(bais)
    val maxWidth = 400
    val maxHeight = 400
    val height = image.height
    val width = image.width
    val ratio = (maxWidth / width).coerceAtMost(maxHeight / height);
    val targetWidth = width * ratio
    val targetHeight = height * ratio
    val scaledImage = BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB)
    scaledImage.graphics.drawImage(image.getScaledInstance(targetWidth, targetHeight, Image.SCALE_DEFAULT), 0, 0, null)
    val baot = ByteArrayOutputStream()
    ImageIO.write(scaledImage, imageFormat, baot)
    return baot.toByteArray()
  }
}
