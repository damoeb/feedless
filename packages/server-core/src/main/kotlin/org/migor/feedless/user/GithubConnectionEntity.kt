package org.migor.feedless.user

import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity

@Entity
@DiscriminatorValue("github")
open class GithubConnectionEntity : ConnectedAppEntity() {
  @Column(name = "github_id")
  open var githubId: String? = null
}
