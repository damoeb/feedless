package org.migor.rich.rss.data.es.documents

import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.validation.constraints.NotNull
import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType
import java.util.*

enum class ContentDocumentType {
  NATIVE_FEED,
  BUCKET,
  CONTENT

}

@Document(indexName = "content")
class FulltextDocument {
  @Id
  var id: UUID? = null

  @Field(type = FieldType.Text)
  var title: String? = null

  @NotNull
  @Field(type = FieldType.Keyword)
  @Enumerated(EnumType.STRING)
  var type: ContentDocumentType? = null

  @Field(type = FieldType.Text)
  var body: String? = null

  @Field(type = FieldType.Text, includeInParent = true)
  var url: String? = null
}
