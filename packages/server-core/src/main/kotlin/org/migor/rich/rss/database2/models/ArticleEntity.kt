package org.migor.rich.rss.database2.models

import org.migor.rich.rss.database2.EntityWithUUID
import javax.persistence.Basic
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table
open class ArticleEntity: EntityWithUUID() {

    @Basic
    @Column(name = "updatedAt", nullable = false)
    open var updatedAt: java.sql.Timestamp? = null

    @Basic
    @Column(name = "date_published", nullable = false)
    open var publishedAt: java.sql.Timestamp? = null

    @Basic
    @Column(name = "date_modified")
    open var modifiedAt: java.sql.Timestamp? = null

    @Basic
    @Column(name = "url")
    open var url: String? = null

    @Basic
    @Column(name = "title", nullable = false)
    open var title: String? = null

    @Basic
    @Column(name = "content_raw_mime")
    open var contentRawMime: String? = null

    @Basic
    @Column(name = "content_raw")
    open var contentRaw: String? = null

    @Basic
    @Column(name = "content_text")
    open var contentText: String? = null

//    @Basic
//    @Column(name = "enclosure")
//    open var enclosure: Any? = null

    @Basic
    @Column(name = "main_image_url")
    open var mainImageUrl: String? = null

}

