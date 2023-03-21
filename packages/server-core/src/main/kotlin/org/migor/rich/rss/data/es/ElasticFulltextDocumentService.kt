package org.migor.rich.rss.data.es

import org.migor.rich.rss.AppProfiles
import org.migor.rich.rss.data.es.documents.FulltextDocument
import org.migor.rich.rss.data.es.repositories.FulltextDocumentRepository
import org.migor.rich.rss.generated.types.BucketsWhereInput
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.PageRequest
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate
import org.springframework.stereotype.Service
import java.util.*


@Service
@Profile(AppProfiles.elastic)
class ElasticFulltextDocumentService(var fulltextDocumentRepository: FulltextDocumentRepository, var esTemplate: ElasticsearchTemplate): FulltextDocumentService {

  private val log = LoggerFactory.getLogger(ElasticFulltextDocumentService::class.simpleName)

  override fun save(doc: FulltextDocument) {
    fulltextDocumentRepository.save(doc)
  }

  override fun saveAll(docs: List<FulltextDocument>) {
    fulltextDocumentRepository.saveAll(docs)
  }

  override fun deleteById(id: UUID) {
    fulltextDocumentRepository.deleteById(id)
  }

  override fun search(query: BucketsWhereInput, pageable: PageRequest): List<FulltextDocument> {
//    val esQuery = NativeQueryBuilder()
////        .withFilter(QueryBuilders.termQuery("type", ContentDocumentType.BUCKET.name))
//      .withFields("id")
////        .withQuery(QueryBuilders.queryStringQuery(query))
//      .withMaxResults(pageable.pageSize)
//      .build()
//
//    return esTemplate.search(esQuery, ContentDocument::class.java)
    return emptyList()
  }
}
