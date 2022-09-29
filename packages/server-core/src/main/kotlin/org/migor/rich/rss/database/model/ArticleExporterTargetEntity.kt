package org.migor.rich.rss.database.model

import javax.persistence.Basic
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "ArticleExporterTarget", schema = "public", catalog = "rich-rss")
open class ArticleExporterTargetEntity {
    @get:Id
    @get:Column(name = "id", nullable = false)
    var id: String? = null

    @get:Basic
    @get:Column(name = "type", nullable = false)
    var type: String? = null

    @get:Basic
    @get:Column(name = "context", nullable = true)
    var context: String? = null

    @get:Basic
    @get:Column(name = "exporterId", nullable = false, insertable = false, updatable = false)
    var exporterId: String? = null

    @get:ManyToOne(fetch = FetchType.LAZY)
    @get:JoinColumn(name = "exporterId", referencedColumnName = "id")
    var refArticleExporterEntity: ArticleExporterEntity? = null

    override fun toString(): String =
        "Entity of type: ${javaClass.name} ( " +
                "id = $id " +
                "type = $type " +
                "context = $context " +
                "exporterId = $exporterId " +
                ")"

    // constant value returned to avoid entity inequality to itself before and after it's update/merge
    override fun hashCode(): Int = 42

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as ArticleExporterTargetEntity

        if (id != other.id) return false
        if (type != other.type) return false
        if (context != other.context) return false
        if (exporterId != other.exporterId) return false

        return true
    }

}

