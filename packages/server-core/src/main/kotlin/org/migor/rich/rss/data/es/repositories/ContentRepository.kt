package org.migor.rich.rss.data.es.repositories

import org.migor.rich.rss.AppProfiles
import org.migor.rich.rss.data.es.documents.ContentDocument
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.elasticsearch.annotations.Query
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository
import java.util.*

@Profile(AppProfiles.elasticsearch)
interface ContentRepository: ElasticsearchRepository<ContentDocument, UUID> {

  @Query("""
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
  """)
  fun findAllByType(query: String, type: String, pageable: Pageable): Page<ContentDocument>
}

