package org.migor.feedless.data.jpa.connectedApp

import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import org.migor.feedless.connectedApp.GithubConnection

@Entity
@DiscriminatorValue("github")
open class GithubConnectionEntity : ConnectedAppEntity() {
  @Column(name = "github_id")
  open var githubId: String? = null
}

fun GithubConnectionEntity.toDomain(): GithubConnection {
  return GithubConnectionMapper.INSTANCE.toDomain(this)
}

fun GithubConnection.toEntity(): GithubConnectionEntity {
  return GithubConnectionMapper.INSTANCE.toEntity(this)
}
