package org.migor.feedless.plugins

import jakarta.mail.util.ByteArrayDataSource
import org.apache.commons.lang3.BooleanUtils
import org.apache.commons.text.similarity.LevenshteinDistance
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.ApiUrls
import org.migor.feedless.data.jpa.enums.ReleaseStatus
import org.migor.feedless.data.jpa.models.MailForwardEntity
import org.migor.feedless.data.jpa.models.SourceSubscriptionEntity
import org.migor.feedless.data.jpa.models.WebDocumentEntity
import org.migor.feedless.data.jpa.repositories.ScrapeSourceDAO
import org.migor.feedless.data.jpa.repositories.WebDocumentDAO
import org.migor.feedless.generated.types.FeedlessPlugins
import org.migor.feedless.generated.types.PluginExecutionParamsInput
import org.migor.feedless.generated.types.WebDocumentField
import org.migor.feedless.service.ProductService
import org.migor.feedless.service.TemplateService
import org.migor.feedless.service.VisualDiffChangeDetectedMailTemplate
import org.migor.feedless.service.VisualDiffChangeDetectedParams
import org.migor.feedless.service.VisualDiffWelcomeMailTemplate
import org.migor.feedless.service.VisualDiffWelcomeParams
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.imageio.ImageIO
import kotlin.math.abs

fun getLastWebDocumentBySubscription(webDocumentDAO: WebDocumentDAO, subscriptionId: UUID): WebDocumentEntity? {
  val pageable = PageRequest.of(0, 1, Sort.Direction.DESC, "createdAt")
  return webDocumentDAO.findAllBySubscriptionIdAndStatusAndReleasedAtBefore(
    subscriptionId,
    ReleaseStatus.released,
    Date(),
    pageable
  ).firstOrNull()
}

@Service
@Profile(AppProfiles.database)
class DiffDataForwarderPlugin : FilterEntityPlugin, MailProviderPlugin {

  private val log = LoggerFactory.getLogger(DiffDataForwarderPlugin::class.simpleName)

  @Autowired
  lateinit var webDocumentDAO: WebDocumentDAO

  @Autowired
  lateinit var productService: ProductService

  @Autowired
  lateinit var scrapeSourceDAO: ScrapeSourceDAO

  @Autowired
  lateinit var templateService: TemplateService

  override fun id() = FeedlessPlugins.org_feedless_diff_email_forward.name
  override fun name() = ""
  override fun listed() = false

  override fun filterEntity(
    corrId: String,
    webDocument: WebDocumentEntity,
    params: PluginExecutionParamsInput
  ): Boolean {
    log.info("[$corrId] filter ${webDocument.url}")

    val increment = params.diffEmailForward.nextItemMinIncrement.coerceAtLeast(0.01)
    log.info("[$corrId] filter nextItemMinIncrement=$increment")

    val previous = getLastWebDocumentBySubscription(webDocumentDAO, webDocument.subscriptionId)

    return previous?.let {
      when (params.diffEmailForward.compareBy!!) {
        WebDocumentField.text -> compareByText(
          corrId,
          webDocument.contentText!!,
          previous.contentText!!,
          increment
        )

        WebDocumentField.markup -> compareByText(
          corrId,
          webDocument.contentHtml!!,
          previous.contentHtml!!,
          increment
        )

        WebDocumentField.pixel -> compareByPixel(
          corrId,
          webDocument,
          previous,
          increment
        )
      }
    } ?: true
  }

  override fun provideWelcomeMail(
    corrId: String,
    subscription: SourceSubscriptionEntity,
    mailForward: MailForwardEntity
  ): MailData {
    log.info("[$corrId] prepare welcome mail")
    val mailData = MailData()
    mailData.subject = "VisualDiff Tracker: ${subscription.title}"
    val website =
      scrapeSourceDAO.findAllBySubscriptionId(subscription.id).map { it.scrapeRequest.page.url }.joinToString(", ")

    val params = VisualDiffWelcomeParams(
      trackerTitle = subscription.title,
      website = website,
      trackerInfo = subscription.schedulerExpression,
      activateTrackerMailsUrl = "${productService.getGatewayUrl(subscription.product)}${ApiUrls.mailForwardingAllow}/${mailForward.id}",
      info = ""
    )
    mailData.body = templateService.renderTemplate(corrId, VisualDiffWelcomeMailTemplate(params))

    return mailData
  }

  override fun provideWebDocumentMail(
    corrId: String,
    webDocument: WebDocumentEntity,
    subscription: SourceSubscriptionEntity,
    params: PluginExecutionParamsInput
  ): MailData {
    log.info("[$corrId] prepare diff email")

    val config = params.diffEmailForward

    val mailData = MailData()

    mailData.subject = subscription.title

    val sdf = SimpleDateFormat("yyyy/MM/dd-HH:mm")

    val latestDateString = sdf.format(webDocument.createdAt)
    val images = mutableListOf(
      Triple(config.inlineLatestImage, "latestImage-$latestDateString", webDocument.contentRaw!!),
    )

    val previous = getLastWebDocumentBySubscription(this.webDocumentDAO, subscription.id)
    previous?.let {
      images.add(
        Triple(
          config.inlinePreviousImage,
          "previousImage-${sdf.format(previous.createdAt)}",
          previous.contentRaw!!
        )
      )
      images.add(
        Triple(
          config.inlineDiffImage,
          "diffImage-${latestDateString}",
          createDiffImage(previous, webDocument)
        )
      )
    }

    val inlined = mutableListOf<String>()
    images.forEach { (shouldInline, contentId, data) ->
      run {
        if (BooleanUtils.isTrue(shouldInline)) {
          inlined.add("<p><img src='cid:$contentId'></p>")
          mailData.attachments.add(MailAttachment(contentId, ByteArrayDataSource(data, "image/webp"), true))
        }
        mailData.attachments.add(MailAttachment("$contentId.webp", ByteArrayDataSource(data, "image/webp")))
      }
    }

    val website = scrapeSourceDAO.findAllBySubscriptionId(
      subscription.id
    ).joinToString(", ") { s -> s.scrapeRequest.page.url }

    val templateParams = VisualDiffChangeDetectedParams(
      trackerTitle = subscription.title,
      website = website,
      inlineImages = inlined.joinToString("\n")
    )
    mailData.body = templateService.renderTemplate(corrId, VisualDiffChangeDetectedMailTemplate(templateParams))

    return mailData
  }

  private fun toImage(webDocument: WebDocumentEntity): BufferedImage {
    return ImageIO.read(ByteArrayInputStream(webDocument.contentRaw))
  }

  private fun compareByPixel(
    corrId: String,
    left: WebDocumentEntity,
    right: WebDocumentEntity,
    minIncrement: Double
  ): Boolean {
    val img1 = toImage(left)
    val img2 = toImage(right)

    val width = img1.width.coerceAtLeast(img2.width)
    val height = img1.height.coerceAtLeast(img2.height)
    var changes = 0
    for (x in 0 until width) {
      for (y in 0 until height) {
        if (img1.getRGB(x, y) != img2.getRGB(x, y)) {
          changes++
        }
      }
    }

    changes += abs(img1.width - img2.width) * img1.height.coerceAtLeast(img2.height) + abs(img1.height - img2.height) * img1.width.coerceAtLeast(
      img2.width
    )
    val total = (width * height)
    val ratio = changes / total.toDouble()
    log.info("[$corrId] pixelDistance=$changes total=$total ratio=$ratio")
    return ratio > minIncrement
  }

  private fun compareByText(corrId: String, left: String, right: String, minIncrement: Double): Boolean {
    val changes = LevenshteinDistance.getDefaultInstance().apply(left, right)
    val len = left.length.coerceAtLeast(right.length)
    val ratio = changes / len.toDouble()
    log.info("[$corrId] editDistance=$changes total=$len ratio=$ratio")
    return ratio > minIncrement
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
    ImageIO.write(diffImage, "webp", baos)
    return baos.toByteArray()
  }

}
