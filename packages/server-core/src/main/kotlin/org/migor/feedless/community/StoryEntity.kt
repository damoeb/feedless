package org.migor.feedless.community

import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import org.migor.feedless.jpa.repository.RepositoryEntity

@Entity
@DiscriminatorValue("story")
open class StoryEntity : RepositoryEntity()
