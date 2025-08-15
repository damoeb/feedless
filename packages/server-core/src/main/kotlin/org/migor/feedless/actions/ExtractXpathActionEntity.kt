package org.migor.feedless.actions

import jakarta.persistence.Basic
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.PrePersist
import jakarta.persistence.PrimaryKeyJoinColumn
import jakarta.persistence.Table
import jakarta.persistence.Transient
import jakarta.validation.constraints.Size
import org.hibernate.annotations.JdbcTypeCode
import org.migor.feedless.document.DocumentEntity.Companion.LEN_STR_DEFAULT
import org.migor.feedless.generated.types.ScrapeEmit
import java.sql.Types


@Entity
@Table(name = "t_action_extract_xpath")
@PrimaryKeyJoinColumn(
  foreignKey = ForeignKey(
    name = "fk_base_entity",
    foreignKeyDefinition = "FOREIGN KEY (id) REFERENCES t_scrape_action(id) ON DELETE CASCADE"
  )
)
open class ExtractXpathActionEntity : ScrapeActionEntity() {

  @Size(message = "fragmentName", max = LEN_STR_DEFAULT)
  @Column(name = "fragment_name", nullable = false)
  open lateinit var fragmentName: String

  @XPathConstraint
  @Size(message = "xpath", min = 1, max = 100)
  @Column(name = "xpath", nullable = false)
  open lateinit var xpath: String

  @Basic(fetch = FetchType.EAGER)
  @JdbcTypeCode(Types.ARRAY)
  @Column(name = "emit", nullable = false, columnDefinition = "text[]")
  open var emitRaw: Array<String> = emptyArray()

  @Enumerated(EnumType.STRING)
  @Column(name = "unique_by", nullable = false)
  open lateinit var uniqueBy: ExtractEmit

  @Transient
  fun getEmit(): Array<ExtractEmit> {
    return emitRaw.map { ExtractEmit.valueOf(it) }.toTypedArray()
  }

  @Transient
  fun setEmit(emit: Array<ExtractEmit>) {
    emitRaw = emit.map { it.name }.toTypedArray()
  }

  @PrePersist
  fun prePersist() {
    assert(
      emitRaw.contains(uniqueBy.name),
      { "uniqueBy '${uniqueBy}' must be part of emit types [${emitRaw.joinToString(", ")}]" })
  }
}

enum class ExtractEmit {
  text,
  html,
  pixel,
  date
}

fun ScrapeEmit.fromDto(): ExtractEmit {
  return when (this) {
    ScrapeEmit.text -> ExtractEmit.text
    ScrapeEmit.html -> ExtractEmit.html
    ScrapeEmit.pixel -> ExtractEmit.pixel
    ScrapeEmit.date -> ExtractEmit.date
  }
}

fun ExtractEmit.toDto(): ScrapeEmit {
  return when (this) {
    ExtractEmit.text -> ScrapeEmit.text
    ExtractEmit.html -> ScrapeEmit.html
    ExtractEmit.pixel -> ScrapeEmit.pixel
    ExtractEmit.date -> ScrapeEmit.date
  }
}
