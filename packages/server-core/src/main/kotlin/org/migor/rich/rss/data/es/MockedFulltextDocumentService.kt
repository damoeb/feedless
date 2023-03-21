package org.migor.rich.rss.data.es

import org.migor.rich.rss.AppProfiles
import org.migor.rich.rss.data.es.documents.FulltextDocument
import org.migor.rich.rss.generated.types.BucketsWhereInput
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.util.*


@Service
@Profile("!${AppProfiles.elastic}")
class MockedFulltextDocumentService: FulltextDocumentService {
  override fun save(doc: FulltextDocument) {
  }

  override fun saveAll(docs: List<FulltextDocument>) {

  }

  override fun deleteById(id: UUID) {
  }

  override fun search(query: BucketsWhereInput, pageable: PageRequest): List<FulltextDocument> {
    return emptyList()
  }
}
