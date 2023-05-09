package org.migor.feedless.data.jpa.enums

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
