package org.migor.feedless.data.jpa.models

import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.migor.feedless.data.jpa.EntityWithUUID

@Entity
@Table(name = "t_stream")
@Deprecated("obsolete")
open class StreamEntity : EntityWithUUID() {

  @OneToMany(mappedBy = "id", fetch = FetchType.LAZY, cascade = [])
  open var documents: MutableList<WebDocumentEntity>? = mutableListOf()

  @OneToOne(mappedBy = "stream", cascade = [CascadeType.REMOVE], optional = true)
  open var bucket: BucketEntity? = null

}

