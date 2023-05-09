package org.migor.feedless.service

import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.repositories.ArticleDAO
import org.migor.feedless.generated.types.HistogramFrame
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.util.*

@Service
@Profile(AppProfiles.database)
class HistogramService {
  @Autowired
  lateinit var articleDAO: ArticleDAO

  fun histogramByStreamIdOrImporterId(streamId: UUID?, importerId: UUID?, frame: HistogramFrame): List<HistogramRawItem> {
    return articleDAO.histogramPerDayByStreamIdOrImporterId(streamId ?: importerId!!)
      .map { toItem(it) }
  }

  private fun toItem(it: Array<Any>): HistogramRawItem {
    return HistogramRawItem(
      year = (it[0] as Double).toInt(),
      month = (it[1] as Double).toInt(),
      day = (it[2] as Double).toInt(),
      count = (it[3] as Long).toInt(),
    )
  }

}

data class HistogramRawItem(val year: Int, val month: Int, val day: Int, val count: Int)
