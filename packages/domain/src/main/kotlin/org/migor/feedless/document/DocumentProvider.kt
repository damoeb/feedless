package org.migor.feedless.document

import org.migor.feedless.PageableRequest
import org.migor.feedless.capability.CapabilityConsumer
import org.migor.feedless.capability.UnresolvedCapability

interface DocumentProvider : CapabilityConsumer {
  suspend fun provideAll(
    capability: UnresolvedCapability,
    pageable: PageableRequest,
    filter: DocumentsFilter,
    order: RecordOrderBy
  ): List<Document>
}
