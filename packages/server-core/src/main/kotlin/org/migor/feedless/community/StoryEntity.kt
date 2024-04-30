package org.migor.feedless.community

import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity

@Entity
@DiscriminatorValue("story")
open class StoryEntity : PostEntity() {

  @Column(nullable = false, length = 200, name = "title")
  open lateinit var title: String

  @Column(length = 500, name = "link")
  open var link: String? = null
}
