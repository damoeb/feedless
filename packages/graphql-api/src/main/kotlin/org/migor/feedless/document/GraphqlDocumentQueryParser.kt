package org.migor.feedless.document

import com.google.gson.Gson
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.generated.types.RecordOrderByInput
import org.migor.feedless.generated.types.RecordsWhereInput
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("${AppProfiles.repository} & ${AppLayer.api}")
class GraphqlDocumentQueryParser : DocumentQueryParser {

  override fun parseFilter(json: String): DocumentsFilter =
    Gson().fromJson(json, RecordsWhereInput::class.java).toDomain()

  override fun parseOrderBy(json: String): RecordOrderBy =
    Gson().fromJson(json, RecordOrderByInput::class.java).toDomain()
}
