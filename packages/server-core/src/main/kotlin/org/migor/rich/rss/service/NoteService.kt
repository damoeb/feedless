package org.migor.rich.rss.service

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile("database2")
class NoteService {

  private val log = LoggerFactory.getLogger(NoteService::class.simpleName)

//  @Transactional
//  fun createRootNote(corrId: String, userId: String, bucket: Bucket): ArticleRef {
//    val note = Article()
//    note.title = "note"
//    note.contentRaw = "hello"
//    note.contentRawMime = "text/markdown"
//    note.contentText = "hello"
//    note.pubDate = Date()
//
//    val saved = articleRepository.save(note)
//
//    val ref = ArticleRef()
//    ref.streamId = bucket.streamId
//    ref.articleId = saved.id!!
//
//    return articleRefRepository.save(ref)
//  }

}
