package org.migor.feedless.plugins

import jakarta.mail.util.ByteArrayDataSource
import org.apache.commons.lang3.BooleanUtils
import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.models.SourceSubscriptionEntity
import org.migor.feedless.data.jpa.models.WebDocumentEntity
import org.migor.feedless.data.jpa.repositories.UserDAO
import org.migor.feedless.data.jpa.repositories.WebDocumentDAO
import org.migor.feedless.generated.types.FeedlessPlugins
import org.migor.feedless.generated.types.PluginExecutionParamsInput
import org.migor.feedless.mail.MailService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.*
import javax.imageio.ImageIO


@Service
@Profile("${AppProfiles.database} && ${AppProfiles.mail}")
class DiffEmailForwardPlugin: MapEntityPlugin {

  private val log = LoggerFactory.getLogger(DiffEmailForwardPlugin::class.simpleName)

  @Autowired
  private lateinit var javaMailSender: JavaMailSender

  @Autowired
  private lateinit var mailService: MailService

  @Autowired
  private lateinit var userDAO: UserDAO

  @Autowired
  private lateinit var webDocumentDAO: WebDocumentDAO

  override fun id(): String = FeedlessPlugins.org_feedless_diff_email_forward.name

  override fun name() = ""
  override fun description() = ""
  override fun listed() = false

  override fun mapEntity(
    corrId: String,
    webDocument: WebDocumentEntity,
    subscription: SourceSubscriptionEntity,
    params: PluginExecutionParamsInput?
  ) {
    log.info("[$corrId] send diff email")

    val config = params!!.diffEmailForward

    val m = javaMailSender.createMimeMessage()
    val message = MimeMessageHelper(m, true, "UTF-8")
    message.setFrom(mailService.getNoReplyAddress(userDAO.findById(subscription.ownerId).orElseThrow().product))
    message.setTo(config.emailRecipients.toTypedArray())
    message.setSubject(subscription.title)
    var body = "some text"


    val images = mutableListOf(
      Triple(config.inlineLatestImage, "latestImage", webDocument.contentRaw!! ),
    )

    val previous = getLastWebDocumentBySubscription(this.webDocumentDAO, subscription.id)
    previous?.let {
      images.add(Triple(config.inlinePreviousImage, "previousImage", webDocument.contentRaw!!))
      images.add(Triple(config.inlineDiffImage, "diffImage", createDiffImage(previous, webDocument)))
    }

    images.forEach { (shouldInline, contentId, data) ->
      run {
        val resource = ByteArrayDataSource(data, "image/png")
        if (BooleanUtils.isTrue(shouldInline)) {
          body += "<p><img src='cid:$contentId'></p>"
          message.addInline(contentId, resource)
        }
        message.addAttachment("$contentId.png", resource)
      }
    }

    message.setText(body, true)

    javaMailSender.send(m)
  }

  private fun toImage(document: WebDocumentEntity): BufferedImage {
    return ImageIO.read(ByteArrayInputStream(document.contentRaw))
  }

  private fun createDiffImage(left: WebDocumentEntity, right: WebDocumentEntity): ByteArray {
    val img1 = toImage(left)
    val img2 = toImage(right)

    val width = img1.width.coerceAtLeast(img2.width)
    val height = img1.height.coerceAtLeast(img2.height)

    val diffImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)

    for (x in 0 until width) {
      for (y in 0 until height) {
        if (img1.getRGB(x, y) == img2.getRGB(x, y)) {
          diffImage.setRGB(x, y, img2.getRGB(x, y))
        } else {
          diffImage.setRGB(x, y, 0xff0000)
        }
      }
    }

    val baos = ByteArrayOutputStream()
    ImageIO.write(diffImage, "png", baos)
    return baos.toByteArray()
  }
}
