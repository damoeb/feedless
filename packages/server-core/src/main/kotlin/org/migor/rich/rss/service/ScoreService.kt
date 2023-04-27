package org.migor.rich.rss.service

import org.migor.rich.rss.AppProfiles
import org.migor.rich.rss.data.jpa.models.ContentEntity
import org.migor.rich.rss.data.jpa.models.WebDocumentEntity
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile(AppProfiles.database)
class ScoreService {

  fun handleWebDocument(corrId: String, webDocument: WebDocumentEntity) {

  }

  fun handleContent(corrId: String, content: ContentEntity) {

  }

}
