package org.migor.feedless.data.es

import org.migor.feedless.data.es.documents.FulltextDocument
import org.migor.feedless.generated.types.BucketsWhereInput
import org.springframework.data.domain.PageRequest
import java.util.*


interface FulltextDocumentService {

  fun save(doc: FulltextDocument)
  fun saveAll(docs: List<FulltextDocument>)

  fun deleteById(id: UUID)

  fun search(query: BucketsWhereInput, pageable: PageRequest): List<FulltextDocument>
}
