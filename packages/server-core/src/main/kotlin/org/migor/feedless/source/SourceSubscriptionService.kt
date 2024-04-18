package org.migor.feedless.source

import org.apache.commons.lang3.BooleanUtils
import org.apache.commons.lang3.StringUtils
import org.migor.feedless.AppProfiles
import org.migor.feedless.BadRequestException
import org.migor.feedless.NotFoundException
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.actions.ClickPositionActionEntity
import org.migor.feedless.actions.DomActionEntity
import org.migor.feedless.actions.DomEventType
import org.migor.feedless.actions.HeaderActionEntity
import org.migor.feedless.actions.ScrapeActionDAO
import org.migor.feedless.actions.ScrapeActionEntity
import org.migor.feedless.api.dto.RichArticle
import org.migor.feedless.api.dto.RichFeed
import org.migor.feedless.api.fromDto
import org.migor.feedless.common.PropertyService
import org.migor.feedless.config.CacheNames
import org.migor.feedless.data.jpa.StandardJpaFields
import org.migor.feedless.data.jpa.enums.EntityVisibility
import org.migor.feedless.data.jpa.enums.ProductName
import org.migor.feedless.data.jpa.enums.ReleaseStatus
import org.migor.feedless.data.jpa.enums.fromDto
import org.migor.feedless.data.jpa.models.PluginExecution
import org.migor.feedless.data.jpa.models.ScrapeSourceEntity
import org.migor.feedless.data.jpa.models.SourceSubscriptionEntity
import org.migor.feedless.data.jpa.models.WebDocumentEntity
import org.migor.feedless.data.jpa.models.toDto
import org.migor.feedless.data.jpa.repositories.ScrapeSourceDAO
import org.migor.feedless.data.jpa.repositories.SourceSubscriptionDAO
import org.migor.feedless.feed.parser.json.JsonAttachment
import org.migor.feedless.generated.types.PluginExecutionInput
import org.migor.feedless.generated.types.ScrapeAction
import org.migor.feedless.generated.types.ScrapeRequestInput
import org.migor.feedless.generated.types.SourceSubscription
import org.migor.feedless.generated.types.SourceSubscriptionCreateInput
import org.migor.feedless.generated.types.SourceSubscriptionsCreateInput
import org.migor.feedless.generated.types.UpdateSinkOptionsDataInput
import org.migor.feedless.generated.types.Visibility
import org.migor.feedless.mail.MailForwardDAO
import org.migor.feedless.mail.MailForwardEntity
import org.migor.feedless.mail.MailService
import org.migor.feedless.pipeline.PluginService
import org.migor.feedless.plan.PlanConstraintsService
import org.migor.feedless.service.WebDocumentService
import org.migor.feedless.session.SessionService
import org.migor.feedless.user.UserDAO
import org.migor.feedless.user.UserEntity
import org.migor.feedless.user.UserService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Profile(AppProfiles.database)
class SourceSubscriptionService {

  private val log = LoggerFactory.getLogger(SourceSubscriptionService::class.simpleName)

  @Autowired
  lateinit var scrapeSourceDAO: ScrapeSourceDAO

  @Autowired
  lateinit var mailForwardDAO: MailForwardDAO

  @Autowired
  lateinit var scrapeActionDAO: ScrapeActionDAO

  @Autowired
  lateinit var userDAO: UserDAO

  @Autowired
  lateinit var sourceSubscriptionDAO: SourceSubscriptionDAO

  @Autowired(required = false)
  lateinit var mailService: MailService

  @Autowired
  lateinit var sessionService: SessionService

  @Autowired
  lateinit var userService: UserService

  @Autowired
  lateinit var planConstraints: PlanConstraintsService

  @Autowired
  lateinit var webDocumentService: WebDocumentService

  @Autowired
  lateinit var propertyService: PropertyService

  @Autowired
  lateinit var pluginService: PluginService

  @Transactional
  fun create(corrId: String, data: SourceSubscriptionsCreateInput): List<SourceSubscription> {
    log.info("[$corrId] create sourceSubscription with ${data.subscriptions.size} sources")

    val ownerId = getActualUserOrDefaultUser(corrId).id
    val totalCount = sourceSubscriptionDAO.countByOwnerId(ownerId)
    planConstraints.auditScrapeSourceMaxCount(totalCount, ownerId)
    if (planConstraints.violatesScrapeSourceMaxActiveCount(ownerId)) {
      log.info("[$corrId] violates maxActiveCount")
      throw IllegalArgumentException("violates maxActiveCount")
//      log.info("[$corrId] violates maxActiveCount, archiving oldest")
//      sourceSubscriptionDAO.updateArchivedForOldestActive(ownerId)
    }
    return data.subscriptions.map { createSubscription(corrId, ownerId, it).toDto() }
  }

  private fun getActualUserOrDefaultUser(corrId: String): UserEntity {
    return sessionService.userId()?.let {
      sessionService.user(corrId)
    } ?: userService.getAnonymousUser().also { log.info("[$corrId] fallback to user anonymous") }
  }

  private fun createSubscription(
    corrId: String,
    ownerId: UUID,
    subInput: SourceSubscriptionCreateInput
  ): SourceSubscriptionEntity {
    val sub = SourceSubscriptionEntity()

    sub.title = subInput.sinkOptions.title
    sub.description = subInput.sinkOptions.description
    sub.visibility = planConstraints.coerceVisibility(subInput.sinkOptions.visibility?.fromDto())

    planConstraints.auditScrapeRequestMaxCountPerSource(subInput.sources.size, ownerId)
    sub.sources = subInput.sources.map { createScrapeSource(corrId, ownerId, it, sub) }.toMutableList()
    sub.ownerId = ownerId
    subInput.sinkOptions.plugins?.let {
      if (it.size > 5) {
        throw BadRequestException("Too many plugins ${it.size}, limit 5")
      }
      sub.plugins = it.map { plugin -> plugin.fromDto() }
    }
    sub.schedulerExpression = subInput.sourceOptions?.let {
      planConstraints.auditRefreshCron(subInput.sourceOptions.refreshCron)
    } ?: ""
    sub.retentionMaxItems = planConstraints.coerceRetentionMaxItems(subInput.sinkOptions.retention?.maxItems, ownerId)
    sub.retentionMaxAgeDays = planConstraints.coerceRetentionMaxAgeDays(subInput.sinkOptions.retention?.maxAgeDays)
    sub.disabledFrom = planConstraints.coerceScrapeSourceExpiry(corrId, ownerId)
    sub.product = subInput.product.fromDto()

    val saved = sourceSubscriptionDAO.save(sub)

    subInput.additionalSinks?.let { sink ->
      val owner = userDAO.findById(ownerId).orElseThrow()
      sub.mailForwards = sink.mapNotNull { it.email }
        .map { createMailForwarder(corrId, it, sub, owner, sub.product) }
        .toMutableList()
    }

    return saved
  }

  private fun createMailForwarder(
    corrId: String,
    email: String,
    sub: SourceSubscriptionEntity,
    owner: UserEntity,
    product: ProductName
  ): MailForwardEntity {
    val forward = MailForwardEntity()
    forward.email = email
    forward.authorized = email == owner.email
    forward.subscriptionId = sub.id

    val (mailFormatter) = pluginService.resolveMailFormatter(sub)
    val from = mailService.getNoReplyAddress(product)
    mailService.send(corrId, from = from, to = arrayOf(email), mailFormatter.provideWelcomeMail(corrId, sub, forward))

    return mailForwardDAO.save(forward)
  }

  private fun createScrapeSource(
    corrId: String,
    ownerId: UUID,
    req: ScrapeRequestInput,
    sub: SourceSubscriptionEntity
  ): ScrapeSourceEntity {
    val entity = ScrapeSourceEntity()
    val scrapeRequest = req.fromDto()
    log.info("[$corrId] create source ${scrapeRequest.page.url}")
    planConstraints.auditScrapeRequestMaxActions(scrapeRequest.page.actions?.size, ownerId)
    planConstraints.auditScrapeRequestTimeout(scrapeRequest.page.timeout, ownerId)
    entity.emit = scrapeRequest.emit
    entity.url = scrapeRequest.page.url
    entity.timeout = scrapeRequest.page.timeout

    scrapeRequest.page.prerender?.let {
      entity.waitUntil = it.waitUntil
      entity.prerender = true
      entity.additionalWaitSec = it.additionalWaitSec
      entity.viewport = it.viewport
      entity.language = it.language
    }

    entity.debugCookies = BooleanUtils.isTrue(scrapeRequest.debug?.cookies)
    entity.debugHtml = BooleanUtils.isTrue(scrapeRequest.debug?.html)
    entity.debugConsole = BooleanUtils.isTrue(scrapeRequest.debug?.console)
    entity.debugScreenshot = BooleanUtils.isTrue(scrapeRequest.debug?.screenshot)
    entity.debugNetwork = BooleanUtils.isTrue(scrapeRequest.debug?.network)
    entity.subscriptionId = sub.id
    if (scrapeRequest.page.actions != null) {
      entity.actions = scrapeRequest.page.actions
        .map {
          val a = it.fromDto()
          a.scrapeSourceId = entity.id
          a
        }.toMutableList()
    }

    return entity
  }

  @Cacheable(value = [CacheNames.FEED_RESPONSE], key = "\"bucket/\" + #subscriptionId")
  @Transactional(readOnly = true)
  fun getFeedBySubscriptionId(subscriptionId: String, page: Int): RichFeed {
    val id = UUID.fromString(subscriptionId)
    val subscription = sourceSubscriptionDAO.findById(id).orElseThrow { NotFoundException("subscription not found") }
    val items = webDocumentService.findAllBySubscriptionId(id, page, ReleaseStatus.released)
      .map { it.toRichArticle() }

    val richFeed = RichFeed()
    richFeed.id = "subscription:${subscriptionId}"
    richFeed.title = subscription.title
    richFeed.description = subscription.description
    richFeed.websiteUrl = "${propertyService.appHost}/feed/$subscriptionId"
    richFeed.publishedAt = items.maxOfOrNull { it.publishedAt } ?: Date()
    richFeed.items = items
    richFeed.imageUrl = null
    richFeed.expired = false
    richFeed.selfPage = page
    richFeed.feedUrl = "${propertyService.apiGatewayUrl}/feed/${subscriptionId}/atom"

    return richFeed
  }

  fun findAll(offset: Int, pageSize: Int, userId: UUID?): List<SourceSubscriptionEntity> {
    val pageable =
      PageRequest.of(offset, pageSize.coerceAtMost(10), Sort.by(Sort.Direction.DESC, StandardJpaFields.createdAt))
    return (userId
      ?.let { sourceSubscriptionDAO.findAllByOwnerId(it, pageable) }
      ?: emptyList())
  }

  fun findById(corrId: String, id: UUID): SourceSubscriptionEntity {
    val sub = sourceSubscriptionDAO.findById(id).orElseThrow { NotFoundException("not found ($corrId)") }
    return if (sub.visibility === EntityVisibility.isPublic) {
      sub
    } else {
      if (sub.ownerId == getActualUserOrDefaultUser(corrId).id) {
        sub
      } else {
        throw PermissionDeniedException("unauthorized ($corrId)")
      }
    }
  }

  fun delete(corrId: String, id: UUID) {
    val sub = sourceSubscriptionDAO.findById(id).orElseThrow()
    if (sub.ownerId != sessionService.userId()) {
      throw PermissionDeniedException("not authorized")
    }
    sourceSubscriptionDAO.delete(sub)
  }

  fun update(corrId: String, id: UUID, data: UpdateSinkOptionsDataInput): SourceSubscriptionEntity {
    val sub = sourceSubscriptionDAO.findById(id).orElseThrow()
    if (sub.ownerId != sessionService.userId()) {
      throw PermissionDeniedException("not authorized")
    }
    data.sinkOptions?.let {
      it.title?.set?.let { sub.title = it }
      it.description?.set?.let { sub.description = it }
      it.visibility?.set?.let { sub.visibility = planConstraints.coerceVisibility(it.fromDto()) }
      it.plugins?.let {
        sub.plugins = it.map { it.fromDto() }
      }
      it.retention?.let {
        it.maxAgeDays?.set?.let {
          sub.retentionMaxAgeDays = it
        }
        it.maxItems?.set?.let {
          sub.retentionMaxItems = it
        }
      }
    }
    data.sources?.let {
      it.add?.let { it.forEach { createScrapeSource(corrId, sub.ownerId, it, sub) } }
      it.remove?.let {
        scrapeSourceDAO.deleteAllById(it.map { UUID.fromString(it) }
          .filter { sub.sources.any { otherSource -> otherSource.id == it } })
      }
    }

    return sourceSubscriptionDAO.save(sub)
  }
}

private fun ScrapeAction.fromDto(): ScrapeActionEntity {
  return if (this.click?.position !== null) {
    val click = ClickPositionActionEntity()
    click.x = this.click.position.x
    click.y = this.click.position.y
    click
  } else {
    if (this.header !== null) {
      val header = HeaderActionEntity()

      header
    } else {
      this.toDomAction()
    }
  }
}

private fun ScrapeAction.toDomAction(): DomActionEntity {
  val a = DomActionEntity()
  if (this.click != null) {
    a.event = DomEventType.click
    a.xpath = this.click.element.xpath.value
  } else {
    if (this.select != null) {
      a.event = DomEventType.select
      a.xpath = this.select.element.value
      a.data = this.select.selectValue
    } else {
      if (this.purge != null) {
        a.event = DomEventType.purge
        a.xpath = this.purge.value
      } else {
        if (this.type != null) {
          a.event = DomEventType.type
          a.xpath = this.select.element.value
        } else {
          if (this.wait != null) {
            a.event = DomEventType.wait
            a.xpath = this.select.element.value
          } else {
            throw IllegalArgumentException()
          }
        }
      }
    }
  }

  return a
}

private fun Visibility.fromDto(): EntityVisibility {
  return when (this) {
    Visibility.isPublic -> EntityVisibility.isPublic
    Visibility.isPrivate -> EntityVisibility.isPrivate
  }
}

private fun PluginExecutionInput.fromDto(): PluginExecution {
  return PluginExecution(id = this.pluginId, params = this.params)
}

private fun WebDocumentEntity.toRichArticle(): RichArticle {
  val article = RichArticle()
  article.id = this.id.toString()
  article.title = StringUtils.trimToEmpty(this.contentTitle)
  article.url = this.url
  article.attachments = this.attachments.map {
    val a = JsonAttachment()
    a.url = it.url
    a.type = it.type
//                a.size = it.size
//        a.duration = it.duration
    a
  }
  article.contentText = StringUtils.trimToEmpty(this.contentText)
  article.contentRawBase64 = this.contentRaw?.let { Base64.getEncoder().encodeToString(this.contentRaw) }
  article.contentRawMime = this.contentRawMime
  article.publishedAt = this.releasedAt
  article.startingAt = this.startingAt
  article.imageUrl = this.imageUrl
  return article

}
