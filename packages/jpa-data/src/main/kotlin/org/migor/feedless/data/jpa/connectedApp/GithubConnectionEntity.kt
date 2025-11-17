package org.migor.feedless.data.jpa.connectedApp

import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity

@Entity
@DiscriminatorValue("github")
open class GithubConnectionEntity : ConnectedAppEntity() {
  @Column(name = "github_id")
  open var githubId: String? = null
}

fun GithubConnectionEntity.toDomain(): org.migor.feedless.connectedApp.GithubConnection {
  return GithubConnectionMapper.Companion.INSTANCE.toDomain(this)
}
