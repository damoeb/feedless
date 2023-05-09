package org.migor.feedless.data.es.repositories

import org.migor.feedless.AppProfiles
import org.migor.feedless.data.es.documents.FulltextDocument
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.elasticsearch.annotations.Query
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository
import java.util.*

@Profile(AppProfiles.elastic)
interface FulltextDocumentRepository : ElasticsearchRepository<FulltextDocument, UUID> {

  @Query(
    """
    {
      "bool": {
        "must": [
          {
            "query_string": {
              "query": "?0"
            }
          }
        ],
        "filter": [
          {
            "term": {
              "type": "?1"
            }
          }
        ]
      }
    }
  """
  )
  fun findAllByType(query: String, type: String, pageable: Pageable): Page<FulltextDocument>
}

