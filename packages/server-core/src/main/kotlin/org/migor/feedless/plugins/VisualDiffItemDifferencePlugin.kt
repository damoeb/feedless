package org.migor.feedless.plugins

import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.enums.ReleaseStatus
import org.migor.feedless.data.jpa.models.WebDocumentEntity
import org.migor.feedless.data.jpa.repositories.WebDocumentDAO
import org.migor.feedless.generated.types.FeedlessPlugins
import org.migor.feedless.util.JsonUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.util.*

data class VisualDiffItemDifferencePluginParams(val minDifferencePercent: Int)

@Service
@Profile(AppProfiles.database)
class VisualDiffItemDifferencePlugin: ReducePlugin<VisualDiffItemDifferencePluginParams> {

  private val log = LoggerFactory.getLogger(VisualDiffItemDifferencePlugin::class.simpleName)

  @Autowired
  lateinit var webDocumentDAO: WebDocumentDAO

  override fun parseParams(params: String): VisualDiffItemDifferencePluginParams {
    return JsonUtil.gson.fromJson(params, VisualDiffItemDifferencePluginParams::class.java)
  }

  override fun reduce(corrId: String, webDocuments: List<WebDocumentEntity>, params: VisualDiffItemDifferencePluginParams): List<WebDocumentEntity> {
    assert(webDocuments.size == 1)
    val next = webDocuments.first()
    val pageable = PageRequest.of(1, 1, Sort.Direction.DESC, "createdAt")
    val previous = webDocumentDAO.findAllBySubscriptionIdAndStatus(next.subscriptionId, ReleaseStatus.released, pageable)

    return if (previous.isEmpty()) {
      listOf(next)
    } else {

      emptyList()
    }
  }

  override fun id(): String = FeedlessPlugins.org_feedless_feed.name

}
