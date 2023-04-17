package org.migor.rich.rss.service

import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.parser.Tag
import org.migor.rich.rss.AppProfiles
import org.migor.rich.rss.data.es.FulltextDocumentService
import org.migor.rich.rss.data.es.documents.ContentDocumentType
import org.migor.rich.rss.data.es.documents.FulltextDocument
import org.migor.rich.rss.data.jpa.models.ContentEntity
import org.migor.rich.rss.data.jpa.repositories.AttachmentDAO
import org.migor.rich.rss.data.jpa.repositories.ContentDAO
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*
import javax.imageio.ImageIO


@Service
@Profile(AppProfiles.database)
class ContentService {

  private val log = LoggerFactory.getLogger(ContentService::class.simpleName)

  @Autowired
  lateinit var contentDAO: ContentDAO

  @Autowired
  lateinit var attachmentDAO: AttachmentDAO

  @Autowired
  lateinit var fulltextDocumentService: FulltextDocumentService

  @Autowired
  lateinit var httpService: HttpService

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  fun save(contentEntity: ContentEntity): ContentEntity {
    val attachments = contentEntity.attachments
    contentEntity.attachments = emptyList()
    contentEntity.hasAudio = attachments.any { it.mimeType!!.startsWith("audio") }
    contentEntity.hasVideo = attachments.any { it.mimeType!!.startsWith("video") }
    val saved = contentDAO.save(contentEntity)
    contentEntity.attachments = attachmentDAO.saveAll(attachments.map {
      run {
        it.content = saved
        it
      }
    })
      .toList()

    saveInElastic(listOf(saved))

    return saved
  }

  private fun saveInElastic(contentEntities: List<ContentEntity>): List<ContentEntity> {
    fulltextDocumentService.saveAll(contentEntities.map { toContentDocument(it) })
    return contentEntities
  }

  private fun toContentDocument(contentEntity: ContentEntity): FulltextDocument {
    val doc = FulltextDocument()
    doc.id = contentEntity.id
    doc.type = ContentDocumentType.CONTENT
    doc.url = contentEntity.url
    doc.title = contentEntity.title
    doc.body = contentEntity.contentText
    return doc
  }

  fun findById(id: UUID): Optional<ContentEntity> {
    return contentDAO.findById(id)
  }

  fun inlineImages(corrId: String, document: Document): String {
    val encoder = Base64.getEncoder()
    document.body().select("img[src]")
      .filter { imageElement -> imageElement.attr("src").startsWith("http") }
      .forEach { imageElement ->
        run {
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
