package org.migor.rss.rich.service

import org.migor.rss.rich.database.model.ArticleRef
import org.migor.rss.rich.database.model.BucketType
import org.migor.rss.rich.database.repository.ArticleRefRepository
import org.migor.rss.rich.database.repository.BucketRepository
import org.migor.rss.rich.database.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class NoteService {

  private val log = LoggerFactory.getLogger(NoteService::class.simpleName)

  @Autowired
  lateinit var userRepository: UserRepository

  @Autowired
  lateinit var bucketRepository: BucketRepository

  @Autowired
  lateinit var articleRefRepository: ArticleRefRepository

  @Transactional
  fun createRootNote(corrId: String, userId: String): ArticleRef {
    val archiveBucket = Optional.ofNullable(bucketRepository.findFirstByTypeAndOwnerId(BucketType.ARCHIVE, userId)).orElseThrow()

    val note = ArticleRef()
    note.streamId = archiveBucket.streamId


    return articleRefRepository.save(note)
  }

}
