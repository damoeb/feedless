package org.migor.rich.rss.database2.models

import org.migor.rich.rss.database2.EntityWithUUID
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.OneToMany
import javax.persistence.OneToOne
import javax.persistence.Table

@Entity
@Table
open class StreamEntity: EntityWithUUID() {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feedId", referencedColumnName = "id")
    open var feed: FeedEntity? = null

    @OneToMany(mappedBy = "id", fetch = FetchType.LAZY)
    open var articleRefs: MutableList<Stream2ArticleEntity>? = mutableListOf()

}

