package org.migor.feedless.jpa.user

import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import org.migor.feedless.jpa.connectedApp.ConnectedAppEntity

@Entity
@DiscriminatorValue("github")
open class GithubConnectionEntity : ConnectedAppEntity() {
  @Column(name = "github_id")
  open var githubId: String? = null
}
