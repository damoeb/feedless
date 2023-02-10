package org.migor.rich.rss.graphql

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import kotlinx.coroutines.coroutineScope
import org.migor.rich.rss.AppProfiles
import org.migor.rich.rss.generated.ContentDto
import org.migor.rich.rss.generated.EnclosureDto
import org.migor.rich.rss.service.AttachmentService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@DgsComponent
@Profile(AppProfiles.database)
class ContentDataResolver {

  @Autowired
  lateinit var attachmentService: AttachmentService

  @DgsData(parentType = "Content")
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun enclosures(dfe: DgsDataFetchingEnvironment): List<EnclosureDto> = coroutineScope {
    val content: ContentDto = dfe.getSource()
    attachmentService.findContentById(UUID.fromString(content.id)).map {
      EnclosureDto.builder().setUrl(it.url).setType(it.mimeType).build()
    }
  }
}
