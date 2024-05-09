package org.migor.feedless.community

import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import org.migor.feedless.document.DocumentEntity

@Entity
@DiscriminatorValue("story")
open class StoryEntity : DocumentEntity() {

}
