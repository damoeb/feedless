package org.migor.feedless.actions

import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorColumn
import jakarta.persistence.DiscriminatorType
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.Inheritance
import jakarta.persistence.InheritanceType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.migor.feedless.data.jpa.EntityWithUUID
import org.migor.feedless.data.jpa.StandardJpaFields
import org.migor.feedless.data.jpa.models.SourceEntity
import org.migor.feedless.generated.types.DOMActionSelect
import org.migor.feedless.generated.types.DOMActionType
import org.migor.feedless.generated.types.DOMElement
import org.migor.feedless.generated.types.DOMElementByNameOrXPath
import org.migor.feedless.generated.types.DOMElementByXPath
import org.migor.feedless.generated.types.ScrapeAction
import org.migor.feedless.generated.types.WaitAction
import org.migor.feedless.generated.types.XYPosition
import java.util.*

@Entity
@Table(name = "t_browser_action")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
  name = "action_type",
  discriminatorType = DiscriminatorType.STRING
)
open class BrowserActionEntity : EntityWithUUID() {
  @Column(name = StandardJpaFields.sourceId, nullable = false)
  open lateinit var sourceId: UUID

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(
    name = StandardJpaFields.sourceId,
    referencedColumnName = StandardJpaFields.id,
    insertable = false,
    updatable = false,
    foreignKey = ForeignKey(name = "fk_source__to__browser_action")
  )
  open var source: SourceEntity? = null
}

private fun DomActionEntity.toTypeActionDto(): DOMActionType {
  return DOMActionType.newBuilder()
    .typeValue(data)
    .element(this.toXpathDto())
    .build()
}


private fun DomActionEntity.toClickActionDto(): DOMElement {
  return DOMElement.newBuilder()
    .element(
      DOMElementByNameOrXPath.newBuilder()
        .xpath(toXpathDto())
        .build()
    )
    .build()
}

private fun DomActionEntity.toWaitActionDto(): WaitAction {
  return WaitAction.newBuilder()
    .element(
      DOMElementByNameOrXPath.newBuilder()
        .xpath(toXpathDto())
        .build()
    )
    .build()
}

fun BrowserActionEntity.toDto(): ScrapeAction {
  val builder = ScrapeAction.newBuilder()
  return when (this) {
    is DomActionEntity -> when (event) {
      DomEventType.select -> builder.select(toSelectActionDto()).build()
      DomEventType.type -> builder.type(toTypeActionDto()).build()
      DomEventType.purge -> builder.purge(toXpathDto()).build()
      DomEventType.click -> builder.click(toClickActionDto()).build()
      DomEventType.wait -> builder.wait(toWaitActionDto()).build()
    }

    is ClickPositionActionEntity -> builder.click(
      DOMElement.newBuilder()
        .position(
          XYPosition.newBuilder()
            .x(x)
            .y(y)
            .build()
        )
        .build()
    ).build()

    else -> throw IllegalArgumentException()
  }
}

private fun DomActionEntity.toSelectActionDto(): DOMActionSelect {
  return DOMActionSelect.newBuilder()
    .selectValue(data)
    .element(toXpathDto())
    .build()
}

private fun DomActionEntity.toXpathDto(): DOMElementByXPath {
  return DOMElementByXPath.newBuilder()
    .value(xpath)
    .build()
}
