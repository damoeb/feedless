package org.migor.feedless.document

import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity

@Entity
@DiscriminatorValue("web")
open class DocumentEntity : AbstractDocumentEntity() {

}
