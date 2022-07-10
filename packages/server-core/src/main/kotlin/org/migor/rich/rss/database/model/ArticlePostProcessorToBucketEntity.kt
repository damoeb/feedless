package org.migor.rich.rss.database.model

import javax.persistence.Basic
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "_ArticlePostProcessorToBucket", schema = "public", catalog = "rich-rss")
open class ArticlePostProcessorToBucketEntity {
    @get:Basic
    @get:Column(name = "A", nullable = false, insertable = false, updatable = false)
    var a: String? = null

    @get:Basic
    @get:Column(name = "B", nullable = false, insertable = false, updatable = false)
    var b: String? = null

//    @get:ManyToOne(fetch = FetchType.LAZY)
//    @get:JoinColumn(name = "B", referencedColumnName = "id")
//    var refBucketEntity: BucketEntity? = null

    @get:ManyToOne(fetch = FetchType.LAZY)
    @get:JoinColumn(name = "A", referencedColumnName = "id")
    var refArticlePostProcessorEntity: ArticlePostProcessorEntity? = null

    override fun toString(): String =
        "Entity of type: ${javaClass.name} ( " +
                "a = $a " +
                "b = $b " +
                ")"

    // constant value returned to avoid entity inequality to itself before and after it's update/merge
    override fun hashCode(): Int = 42

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as ArticlePostProcessorToBucketEntity

        if (a != other.a) return false
        if (b != other.b) return false

        return true
    }

}

