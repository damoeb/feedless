package org.migor.feedless.actions

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.Inheritance
import jakarta.persistence.InheritanceType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.migor.feedless.data.jpa.EntityWithUUID
import org.migor.feedless.data.jpa.StandardJpaFields
import org.migor.feedless.generated.types.BoundingBox
import org.migor.feedless.generated.types.DOMActionSelect
import org.migor.feedless.generated.types.DOMActionType
import org.migor.feedless.generated.types.DOMElement
import org.migor.feedless.generated.types.DOMElementByNameOrXPath
import org.migor.feedless.generated.types.DOMElementByXPath
import org.migor.feedless.generated.types.DOMExtract
import org.migor.feedless.generated.types.HttpFetch
import org.migor.feedless.generated.types.HttpGetRequest
import org.migor.feedless.generated.types.PluginExecution
import org.migor.feedless.generated.types.RequestHeader
import org.migor.feedless.generated.types.ScrapeAction
import org.migor.feedless.generated.types.ScrapeBoundingBox
import org.migor.feedless.generated.types.ScrapeExtract
import org.migor.feedless.generated.types.StringLiteralOrVariable
import org.migor.feedless.generated.types.ViewPort
import org.migor.feedless.generated.types.XYPosition
import org.migor.feedless.repository.toDto
import org.migor.feedless.source.SourceEntity
import org.migor.feedless.web.PuppeteerWaitUntil
import java.util.*
import org.migor.feedless.generated.types.PuppeteerWaitUntil as PuppeteerWaitUntilDto

@Entity(name = "t_scrape_action")
@Inheritance(strategy = InheritanceType.JOINED)
open class ScrapeActionEntity : EntityWithUUID() {

  @Column(name = "pos", nullable = false)
  open var pos: Int? = null

  @Column(name = StandardJpaFields.sourceId, nullable = false)
  open lateinit var sourceId: UUID

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(
    name = StandardJpaFields.sourceId,
    referencedColumnName = StandardJpaFields.id,
    insertable = false,
    updatable = false,
    foreignKey = ForeignKey(name = "fk_scrape_action__to__source")
  )
  open var source: SourceEntity? = null
}

private fun DomActionEntity.toTypeActionDto(): DOMActionType {
  return DOMActionType(
    typeValue = data!!,
    element = this.toXpathDto(),
  )
}

fun ScrapeActionEntity.toDto(): ScrapeAction {
  return when (this) {
    is FetchActionEntity -> ScrapeAction(fetch = toHttpFetchActionDto())
    is DomActionEntity -> when (event) {
      DomEventType.select -> ScrapeAction(select = toSelectActionDto())
      DomEventType.type -> ScrapeAction(type = toTypeActionDto())
      DomEventType.purge -> ScrapeAction(purge = toXpathDto())
    }

    is ClickPositionActionEntity -> ScrapeAction(
      click = DOMElement(
        position = XYPosition(
          x = x,
          y = y,
        )
      )
    )

    is HeaderActionEntity -> ScrapeAction(header = toHeaderActionDto())
    is ClickXpathActionEntity -> ScrapeAction(
      click = DOMElement(
        element = DOMElementByNameOrXPath(
          xpath = DOMElementByXPath(value = xpath)
        )
      )
    )

    is ExtractBoundingBoxActionEntity -> ScrapeAction(
      extract = ScrapeExtract(
        fragmentName = fragmentName,
        imageBased = ScrapeBoundingBox(
          BoundingBox(
            x = x,
            y = y,
            w = w,
            h = h
          )
        )
      )
    )

    is ExtractXpathActionEntity -> ScrapeAction(
      extract = ScrapeExtract(
        fragmentName = fragmentName,
        selectorBased = DOMExtract(
          fragmentName = fragmentName,
          xpath = DOMElementByXPath(xpath),
          emit = emit.map { it.toDto() }
        )
      )
    )

    is ExecuteActionEntity -> ScrapeAction(execute = toPluginExecuteActionDto())

    else -> throw IllegalArgumentException("action $this cannot be transformed to dto")
  }
}

private fun HeaderActionEntity.toHeaderActionDto(): RequestHeader {
  return RequestHeader(name = name, value = value)
}

private fun ExecuteActionEntity.toPluginExecuteActionDto(): PluginExecution {
  return PluginExecution(
    pluginId = pluginId,
    params = executorParams!!.toDto()
  )
}

private fun DomActionEntity.toSelectActionDto(): DOMActionSelect {
  return DOMActionSelect(
    selectValue = data!!,
    element = toXpathDto(),
  )
}

private fun FetchActionEntity.toHttpFetchActionDto(): HttpFetch {
  return HttpFetch(
    get = HttpGetRequest(
      url = if (isVariable) {
        StringLiteralOrVariable(variable = url)
      } else {
        StringLiteralOrVariable(literal = url)
      },
      timeout = timeout,
      viewport = ViewPort(
        width = viewportWidth,
        height = viewportHeight,
        isMobile = isMobile,
        isLandscape = isLandscape
      ),
      forcePrerender = forcePrerender,
      language = language,
      waitUntil = waitUntil?.toDto() ?: PuppeteerWaitUntilDto.load,
      additionalWaitSec = additionalWaitSec,
    )
  )
}

private fun PuppeteerWaitUntil.toDto(): PuppeteerWaitUntilDto {
  return when (this) {
    PuppeteerWaitUntil.load -> PuppeteerWaitUntilDto.load
    PuppeteerWaitUntil.networkidle0 -> PuppeteerWaitUntilDto.networkidle0
    PuppeteerWaitUntil.networkidle2 -> PuppeteerWaitUntilDto.networkidle2
    PuppeteerWaitUntil.domcontentloaded -> PuppeteerWaitUntilDto.domcontentloaded
  }
}

private fun DomActionEntity.toXpathDto(): DOMElementByXPath {
  return DOMElementByXPath(
    value = xpath,
  )
}
