package org.migor.rich.rss.data.es

import org.migor.rich.rss.data.es.documents.FulltextDocument
import org.springframework.data.domain.PageRequest
import java.util.*


interface FulltextDocumentService {

  fun save(doc: FulltextDocument)
  fun saveAll(docs: List<FulltextDocument>)

  fun deleteById(id: UUID)

  fun search(query: String, pageable: PageRequest): List<FulltextDocument>
}
