package org.migor.feedless.plugins

import org.apache.commons.text.similarity.LevenshteinDistance
import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.enums.ReleaseStatus
import org.migor.feedless.data.jpa.models.WebDocumentEntity
import org.migor.feedless.data.jpa.repositories.WebDocumentDAO
import org.migor.feedless.generated.types.FeedlessPlugins
import org.migor.feedless.generated.types.PluginExecutionParamsInput
import org.migor.feedless.generated.types.WebDocumentField
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.File
import java.util.*
import javax.imageio.ImageIO
import kotlin.math.abs

@Service
@Profile(AppProfiles.database)
class EnforceItemIncrementPlugin : FilterPlugin {

  private val log = LoggerFactory.getLogger(EnforceItemIncrementPlugin::class.simpleName)

  @Autowired
  lateinit var webDocumentDAO: WebDocumentDAO

  override fun filter(corrId: String, webDocument: WebDocumentEntity, params: PluginExecutionParamsInput): Boolean {
    log.info("[$corrId] filter")
    val pageable = PageRequest.of(0, 1, Sort.Direction.DESC, "createdAt")
    val previous = webDocumentDAO.findAllBySubscriptionIdAndStatus(
      webDocument.subscriptionId,
      ReleaseStatus.released,
      pageable
    )

    return if (previous.isEmpty()) {
      true
    } else {
      when (params.enforceItemIncrement.compareBy!!) {
        WebDocumentField.text -> compareByText(
          corrId,
          webDocument.contentText!!,
          previous.first().contentText!!,
          params.enforceItemIncrement.nextItemMinIncrement
        )

        WebDocumentField.markup -> compareByText(
          corrId,
          webDocument.contentHtml!!,
          previous.first().contentHtml!!,
          params.enforceItemIncrement.nextItemMinIncrement
        )

        WebDocumentField.pixel -> compareByPixel(
          corrId,
          webDocument,
          previous.first(),
          params.enforceItemIncrement.nextItemMinIncrement
        )
      }
    }
  }

  private fun toImage(document: WebDocumentEntity): BufferedImage {
    return ImageIO.read(ByteArrayInputStream(document.contentRaw))
  }

  private fun compareByPixel(corrId: String, left: WebDocumentEntity, right: WebDocumentEntity, minIncrement: Double): Boolean {
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

  override fun id(): String = FeedlessPlugins.org_feedless_enforce_item_increment.name

}
