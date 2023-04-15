package org.migor.rich.rss.graphql

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import kotlinx.coroutines.coroutineScope
import org.migor.rich.rss.AppProfiles
import org.migor.rich.rss.generated.DgsConstants
import org.migor.rich.rss.service.AttachmentService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*
import org.migor.rich.rss.generated.types.Content as ContentDto
import org.migor.rich.rss.generated.types.Enclosure as EnclosureDto

@DgsComponent
@Profile(AppProfiles.database)
class ContentDataResolver {

  @Autowired
  lateinit var attachmentService: AttachmentService

  @DgsData(parentType = DgsConstants.CONTENT.TYPE_NAME)
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun enclosures(dfe: DgsDataFetchingEnvironment): List<EnclosureDto> = coroutineScope {
    val content: ContentDto = dfe.getSource()
    attachmentService.findContentById(UUID.fromString(content.id)).map {
      EnclosureDto.newBuilder()
        .url(it.url)
        .type(it.mimeType)
        .size(it.size)
        .duration(it.duration)
        .build()
    }
  }
}
