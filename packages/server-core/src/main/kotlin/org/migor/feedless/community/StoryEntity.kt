package org.migor.feedless.community

import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import org.migor.feedless.repository.RepositoryEntity

@Entity
@DiscriminatorValue("story")
open class StoryEntity : RepositoryEntity() {

}
