package org.migor.rich.rss.database.model

import javax.persistence.Basic
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.persistence.Table

@Entity
@Table(name = "ArticleExporter", schema = "public", catalog = "rich-rss")
open class ArticleExporterEntity {
    @get:Id
    @get:Column(name = "id", nullable = false, insertable = false, updatable = false)
    var id: String? = null

    @get:Basic
    @get:Column(name = "segment", nullable = false)
    var segment: java.lang.Boolean? = null

    @get:Basic
    @get:Column(name = "segment_sort_field", nullable = true)
    var segmentSortField: String? = null

    @get:Basic
    @get:Column(name = "segment_sort_asc", nullable = false)
    var segmentSortAsc: java.lang.Boolean? = null

    @get:Basic
    @get:Column(name = "segment_size", nullable = true)
    var segmentSize: Int? = null

    @get:Basic
    @get:Column(name = "segment_digest", nullable = false)
    var segmentDigest: java.lang.Boolean? = null

    @get:Basic
    @get:Column(name = "lastUpdatedAt", nullable = true)
    var lastUpdatedAt: java.sql.Timestamp? = null

    @get:Basic
    @get:Column(name = "trigger_refresh_on", nullable = false)
    var triggerRefreshOn: String? = null

    @get:Basic
    @get:Column(name = "trigger_scheduled_last_at", nullable = true)
    var triggerScheduledLastAt: java.sql.Timestamp? = null

    @get:Basic
    @get:Column(name = "trigger_scheduled_next_at", nullable = true)
    var triggerScheduledNextAt: java.sql.Timestamp? = null

    @get:Basic
    @get:Column(name = "trigger_scheduled", nullable = true)
    var triggerScheduled: String? = null

    @get:Basic
    @get:Column(name = "bucketId", nullable = false, insertable = false, updatable = false)
    var bucketId: String? = null

    @get:Basic
    @get:Column(name = "segment_look_ahead_min", nullable = true)
    var segmentLookAheadMin: Int? = null

//    @get:ManyToOne(fetch = FetchType.LAZY)
//    @get:JoinColumn(name = "bucketId", referencedColumnName = "id")
//    var refBucketEntity: BucketEntity? = null

    @get:OneToMany(mappedBy = "refArticleExporterEntity")
    var refArticleExporterTargetEntities: List<ArticleExporterTargetEntity>? = null

    override fun toString(): String =
        "Entity of type: ${javaClass.name} ( " +
                "id = $id " +
                "segment = $segment " +
                "segmentSortField = $segmentSortField " +
                "segmentSortAsc = $segmentSortAsc " +
                "segmentSize = $segmentSize " +
                "segmentDigest = $segmentDigest " +
                "lastUpdatedAt = $lastUpdatedAt " +
                "triggerRefreshOn = $triggerRefreshOn " +
                "triggerScheduledLastAt = $triggerScheduledLastAt " +
                "triggerScheduledNextAt = $triggerScheduledNextAt " +
                "triggerScheduled = $triggerScheduled " +
                "bucketId = $bucketId " +
                "segmentLookAheadMin = $segmentLookAheadMin " +
                ")"

    // constant value returned to avoid entity inequality to itself before and after it's update/merge
    override fun hashCode(): Int = 42

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as ArticleExporterEntity

        if (id != other.id) return false
        if (segment != other.segment) return false
        if (segmentSortField != other.segmentSortField) return false
        if (segmentSortAsc != other.segmentSortAsc) return false
        if (segmentSize != other.segmentSize) return false
        if (segmentDigest != other.segmentDigest) return false
        if (lastUpdatedAt != other.lastUpdatedAt) return false
        if (triggerRefreshOn != other.triggerRefreshOn) return false
        if (triggerScheduledLastAt != other.triggerScheduledLastAt) return false
        if (triggerScheduledNextAt != other.triggerScheduledNextAt) return false
        if (triggerScheduled != other.triggerScheduled) return false
        if (bucketId != other.bucketId) return false
        if (segmentLookAheadMin != other.segmentLookAheadMin) return false

        return true
    }

}

