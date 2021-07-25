package org.migor.rss.rich.cron

import org.migor.rss.rich.harvest.score.ScoreService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service


@Service
class ScoreArticlesCron internal constructor() {

  private val log = LoggerFactory.getLogger(ScoreArticlesCron::class.simpleName)

  @Autowired
  lateinit var scoreService: ScoreService

//  @Autowired
//  lateinit var entryRepository: SourceEntryRepository

  //  @Scheduled(fixedDelay = 4567)
  fun scoreSourceEntries() {
//    val pageable = PageRequest.of(0, 100, Sort.by(Sort.Order.asc("createdAt")))
//    entryRepository.findAllBySentimentNegative(null, pageable)
//      .forEach(Consumer { entry: SourceEntry ->
//        try {
//          log.info("Calculating sentiments for ${ entry.id }")
//          val (positive, neutral, negative) = scoreService.score(entry)
//          log.info("Sentiments for ${ entry.id }: ${ positive }/${ neutral }/${ negative }")
//          entryRepository.updateSentimentById(entry.id!!, positive, neutral, negative)
//
//        } catch (ex: Exception) {
//          log.error("Filed while scoring source-Entry ${entry.id}, ${ex.message}")
//        }
//      })
  }

}

