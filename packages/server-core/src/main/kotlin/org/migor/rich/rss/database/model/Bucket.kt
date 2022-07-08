package org.migor.rich.rss.database.model

import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.Type
import org.springframework.context.annotation.Profile
import javax.persistence.AttributeConverter
import javax.persistence.Basic
import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.Converter
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table
import javax.validation.constraints.NotNull

@Profile("stateful")
@Converter
class BucketTypeConverter : AttributeConverter<BucketType, Int> {
  override fun convertToDatabaseColumn(attribute: BucketType?): Int? {
    return attribute?.id
  }

  override fun convertToEntityAttribute(dbData: Int?): BucketType? {
    return BucketType.findById(dbData)
  }
}

enum class BucketType(val id: Int) {
  NORMAL(0),
  INBOX(1),
  ARCHIVE(2);

  companion object {
    fun findById(id: Int?): BucketType? {
      return values().find { bucketType -> bucketType.id == id }
    }
  }
}

@Profile("stateful")
@Entity
@Table(name = "\"Bucket\"")
class Bucket : JsonSupport() {

  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
  var id: String? = null

  @NotNull
  @Column(name = "\"streamId\"")
  lateinit var streamId: String

  @NotNull
  @Column(name = "title")
  lateinit var name: String

  @NotNull
  @Column(name = "type")
  @Convert(converter = BucketTypeConverter::class)
  var type: BucketType = BucketType.NORMAL

  @Column(name = "description")
  var description: String? = null

  @NotNull
  @Column(name = "\"ownerId\"")
  lateinit var ownerId: String

  @NotNull
  @Column(name = "is_public")
  var isPublic: Boolean = true

  @Column(name = "tags", columnDefinition = "JSONB")
  @Type(type = "jsonb")
  @Basic(fetch = FetchType.LAZY)
  var tags: List<String>? = null

//  @ManyToOne
//  lateinit var owner: User

//  @Temporal(TemporalType.TIMESTAMP)
//  @Column(name = "\"lastUpdatedAt\"")
//  var lastUpdatedAt: Date? = null

}
