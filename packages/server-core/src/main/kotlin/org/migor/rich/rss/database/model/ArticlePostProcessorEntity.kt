package org.migor.rich.rss.database.model

import javax.persistence.Basic
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.persistence.Table

@Entity
@Table(name = "ArticlePostProcessor", schema = "public", catalog = "rich-rss")
open class ArticlePostProcessorEntity {
    @get:Id
    @get:Column(name = "id", nullable = false, insertable = false, updatable = false)
    var id: String? = null

    @get:Basic
    @get:Column(name = "createdAt", nullable = false)
    var createdAt: java.sql.Timestamp? = null

    @get:Basic
    @get:Column(name = "updatedAt", nullable = false)
    var updatedAt: java.sql.Timestamp? = null

    @get:Basic
    @get:Column(name = "type", nullable = false)
    var type: String? = null

    @get:Basic
    @get:Column(name = "context", nullable = true)
    var context: String? = null

    @get:OneToMany(mappedBy = "refArticlePostProcessorEntity")
    var refArticlePostProcessorToBucketEntities: List<ArticlePostProcessorToBucketEntity>? = null

    override fun toString(): String =
        "Entity of type: ${javaClass.name} ( " +
                "id = $id " +
                "createdAt = $createdAt " +
                "updatedAt = $updatedAt " +
                "type = $type " +
                "context = $context " +
                ")"

    // constant value returned to avoid entity inequality to itself before and after it's update/merge
    override fun hashCode(): Int = 42

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as ArticlePostProcessorEntity

        if (id != other.id) return false
        if (createdAt != other.createdAt) return false
        if (updatedAt != other.updatedAt) return false
        if (type != other.type) return false
        if (context != other.context) return false

        return true
    }

}

