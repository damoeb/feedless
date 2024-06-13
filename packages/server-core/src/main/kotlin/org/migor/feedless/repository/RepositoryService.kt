package org.migor.feedless.repository

import org.apache.commons.lang3.BooleanUtils
import org.apache.commons.lang3.StringUtils
import org.migor.feedless.AppProfiles
import org.migor.feedless.BadRequestException
import org.migor.feedless.NotFoundException
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.actions.BrowserActionEntity
import org.migor.feedless.actions.ClickPositionActionEntity
import org.migor.feedless.actions.DomActionEntity
import org.migor.feedless.actions.DomEventType
import org.migor.feedless.actions.HeaderActionEntity
import org.migor.feedless.api.dto.RichArticle
import org.migor.feedless.api.dto.RichFeed
import org.migor.feedless.api.fromDto
import org.migor.feedless.attachment.createAttachmentUrl
import org.migor.feedless.common.PropertyService
import org.migor.feedless.config.CacheNames
import org.migor.feedless.data.jpa.enums.EntityVisibility
import org.migor.feedless.data.jpa.enums.ProductCategory
import org.migor.feedless.data.jpa.enums.ReleaseStatus
import org.migor.feedless.data.jpa.enums.fromDto
import org.migor.feedless.data.jpa.models.SourceEntity
import org.migor.feedless.data.jpa.repositories.SourceDAO
import org.migor.feedless.document.DocumentEntity
import org.migor.feedless.document.DocumentService
import org.migor.feedless.document.createDocumentUrl
import org.migor.feedless.feed.parser.json.JsonAttachment
import org.migor.feedless.generated.types.PluginExecutionInput
import org.migor.feedless.generated.types.RepositoriesCreateInput
import org.migor.feedless.generated.types.RepositoriesWhereInput
import org.migor.feedless.generated.types.Repository
import org.migor.feedless.generated.types.RepositoryCreateInput
import org.migor.feedless.generated.types.RepositoryUpdateDataInput
import org.migor.feedless.generated.types.ScrapeAction
import org.migor.feedless.generated.types.ScrapeRequestInput
import org.migor.feedless.generated.types.Visibility
import org.migor.feedless.mail.MailForwardDAO
import org.migor.feedless.mail.MailForwardEntity
import org.migor.feedless.mail.MailService
import org.migor.feedless.pipeline.PluginService
import org.migor.feedless.plan.PlanConstraintsService
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
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

@Service
@Profile(AppProfiles.database)
class RepositoryService {

  private val log = LoggerFactory.getLogger(RepositoryService::class.simpleName)

  @Autowired
  private lateinit var sourceDAO: SourceDAO

  @Autowired
  private lateinit var mailForwardDAO: MailForwardDAO

  @Autowired
  private lateinit var userDAO: UserDAO

  @Autowired
  private lateinit var repositoryDAO: RepositoryDAO

  @Autowired(required = false)
  lateinit var mailService: MailService

  @Autowired
  private lateinit var sessionService: SessionService

  @Autowired
  private lateinit var userService: UserService

  @Autowired
  private lateinit var planConstraints: PlanConstraintsService

  @Autowired
  private lateinit var documentService: DocumentService

  @Autowired
  private lateinit var propertyService: PropertyService

  @Autowired
  private lateinit var pluginService: PluginService

  @Transactional
  fun create(corrId: String, data: RepositoriesCreateInput): List<Repository> {
    log.info("[$corrId] create repository with ${data.repositories.size} sources")

    val ownerId = getActualUserOrDefaultUser(corrId).id
    val totalCount = repositoryDAO.countByOwnerId(ownerId)
    planConstraints.auditScrapeSourceMaxCount(totalCount, ownerId)
    if (planConstraints.violatesScrapeSourceMaxActiveCount(ownerId)) {
      log.info("[$corrId] violates maxActiveCount")
      throw IllegalArgumentException("violates maxActiveCount")
//      log.info("[$corrId] violates maxActiveCount, archiving oldest")
//      RepositoryDAO.updateArchivedForOldestActive(ownerId)
    }
    return data.repositories.map { createSubscription(corrId, ownerId, it).toDto() }
  }

  private fun getActualUserOrDefaultUser(corrId: String): UserEntity {
    return sessionService.userId()?.let {
      sessionService.user(corrId)
    } ?: userService.getAnonymousUser().also { log.info("[$corrId] fallback to user anonymous") }
  }

  private fun createSubscription(
      corrId: String,
      ownerId: UUID,
      subInput: RepositoryCreateInput
  ): RepositoryEntity {
    val sub = RepositoryEntity()

    sub.title = subInput.sinkOptions.title
    sub.description = subInput.sinkOptions.description
    sub.visibility = planConstraints.coerceVisibility(corrId, subInput.sinkOptions.visibility?.fromDto())

    planConstraints.auditScrapeRequestMaxCountPerSource(subInput.sources.size, ownerId)
    sub.sources = subInput.sources.map { createScrapeSource(corrId, ownerId, it, sub) }.toMutableList()
    sub.ownerId = ownerId
    subInput.sinkOptions.plugins?.let {
      if (it.size > 5) {
        throw BadRequestException("Too many plugins ${it.size}, limit 5")
      }
      sub.plugins = it.map { plugin -> plugin.fromDto() }
    }
    sub.sourcesSyncExpression = subInput.sourceOptions?.let {
      planConstraints.auditCronExpression(subInput.sourceOptions.refreshCron)
    } ?: ""
    sub.retentionMaxItems = planConstraints.coerceRetentionMaxItems(subInput.sinkOptions.retention?.maxItems, ownerId)
    sub.retentionMaxAgeDays = planConstraints.coerceRetentionMaxAgeDays(subInput.sinkOptions.retention?.maxAgeDays, ownerId)
    sub.product = subInput.product.fromDto()

    val saved = repositoryDAO.save(sub)

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
    sub: RepositoryEntity,
    owner: UserEntity,
    product: ProductCategory
  ): MailForwardEntity {
    val forward = MailForwardEntity()
    forward.email = email
    forward.authorized = email == owner.email
    forward.repositoryId = sub.id

    val (mailFormatter) = pluginService.resolveMailFormatter(sub)
    val from = mailService.getNoReplyAddress(product)
    mailService.send(corrId, from = from, to = arrayOf(email), mailFormatter.provideWelcomeMail(corrId, sub, forward))

    return mailForwardDAO.save(forward)
  }

  private fun createScrapeSource(
      corrId: String,
      ownerId: UUID,
      req: ScrapeRequestInput,
      sub: RepositoryEntity
  ): SourceEntity {
    val entity = SourceEntity()
    val scrapeRequest = req.fromDto()
    log.info("[$corrId] create source ${scrapeRequest.page.url}")
    planConstraints.auditScrapeRequestMaxActions(scrapeRequest.page.actions?.size, ownerId)
    planConstraints.auditScrapeRequestTimeout(scrapeRequest.page.timeout, ownerId)
    entity.emit = scrapeRequest.emit
    entity.url = scrapeRequest.page.url
    entity.tags = scrapeRequest.tags?.toTypedArray()
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
    entity.repositoryId = sub.id
    if (scrapeRequest.page.actions != null) {
      entity.actions = scrapeRequest.page.actions
        .map {
          val a = it.fromDto()
          a.sourceId = entity.id
          a
        }.toMutableList()
    }

    return entity
  }

  @Cacheable(value = [CacheNames.FEED_RESPONSE], key = "\"repo/\" + #repositoryId + #tag")
  @Transactional(readOnly = true)
  fun getFeedByRepositoryId(repositoryId: String, page: Int, tag: String? = null): RichFeed {
    val id = UUID.fromString(repositoryId)
    val repository = repositoryDAO.findById(id).orElseThrow { NotFoundException("repository not found") }
    val pageResult = documentService.findAllByRepositoryId(id, page, 10, status = ReleaseStatus.released, tag)
    val items = pageResult.get().map { it.toRichArticle(propertyService, repository.visibility) }.toList()

    val tags = repository.sources.mapNotNull { it.tags?.asList() }.flatten().distinct()

    val title = if(repository.visibility === EntityVisibility.isPublic) {
      repository.title
    } else {
      "${repository.title} (Personal use)"
    }

    val richFeed = RichFeed()
    richFeed.id = "repository:${repositoryId}"
    richFeed.tags = tags
    richFeed.title = title
    richFeed.description = repository.description
    richFeed.websiteUrl = "${propertyService.appHost}/feeds/$repositoryId"
    richFeed.publishedAt = items.maxOfOrNull { it.publishedAt } ?: Date()
    richFeed.items = items
    richFeed.imageUrl = null
    richFeed.expired = false
    richFeed.feedUrl = "${propertyService.apiGatewayUrl}/f/${repositoryId}/atom"

    if (!pageResult.isLast) {
      richFeed.nextUrl = "${propertyService.apiGatewayUrl}/f/${repositoryId}/atom?page=${page+1}"
    }
    if (!pageResult.isFirst) {
      richFeed.previousUrl = "${propertyService.apiGatewayUrl}/f/${repositoryId}/atom?page=${page-1}"
    }

    return richFeed
  }

  fun findAll(offset: Int, pageSize: Int, where: RepositoriesWhereInput?, userId: UUID?): List<RepositoryEntity> {
    val pageable =
        PageRequest.of(offset, pageSize.coerceAtMost(10), Sort.by(Sort.Direction.DESC, "createdAt"))
    return (userId
      ?.let { repositoryDAO.findAllByOwnerId(it, pageable) }
      ?: repositoryDAO.findAllByVisibility(EntityVisibility.isPublic, pageable))
  }

  fun findById(corrId: String, id: UUID): RepositoryEntity {
    val sub = repositoryDAO.findById(id).orElseThrow { NotFoundException("not found ($corrId)") }
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
    val sub = repositoryDAO.findById(id).orElseThrow()
    if (sub.ownerId != sessionService.userId()) {
      throw PermissionDeniedException("not authorized")
    }
    repositoryDAO.delete(sub)
  }

  fun calculateScheduledNextAt(cron: String, ownerId: UUID, after: LocalDateTime): Date {
    return planConstraints.coerceMinScheduledNextAt(
      Date(),
      nextCronDate(cron, after),
      ownerId
    )
  }


  fun update(corrId: String, id: UUID, data: RepositoryUpdateDataInput): RepositoryEntity {
    val repository = repositoryDAO.findById(id).orElseThrow()
    if (repository.ownerId != sessionService.userId()) {
      throw PermissionDeniedException("not authorized")
    }
    data.title?.set?.let { repository.title = it }
    data.description?.set?.let { repository.description = it }
    data.refreshCron?.let {
      it.set?.let {
        repository.sourcesSyncExpression = planConstraints.auditCronExpression(it)
        repository.triggerScheduledNextAt = calculateScheduledNextAt(it, repository.ownerId,
          LocalDateTime.ofInstant(repository.lastUpdatedAt.toInstant(), ZoneId.systemDefault())
        )
      }
    }
    data.visibility?.set?.let { repository.visibility = planConstraints.coerceVisibility(corrId, it.fromDto()) }
    data.plugins?.let { plugins ->
      val newPlugins = plugins.map { it.fromDto() }.sortedBy { it.id }.toMutableList()
      if (newPlugins != repository.plugins) {
        repository.plugins = newPlugins
      }
    }
    data.nextUpdateAt?.let {
      val next = it.set?.let { Date(it) } ?: Date(0)
      repository.triggerScheduledNextAt = planConstraints.coerceMinScheduledNextAt(
        repository.lastUpdatedAt,
        next,
        repository.ownerId
      )
    }
    data.retention?.let {
      it.maxAgeDays?.set?.let {
        repository.retentionMaxAgeDays = it
      }
      it.maxItems?.set?.let {
        repository.retentionMaxItems = it
      }
      if (it.maxAgeDays!=null || it.maxItems!=null) {
        documentService.applyRetentionStrategy(corrId, repository)
      }
    }

    data.sources?.let {
      it.add?.let {
        repository.sources.addAll(it.map { createScrapeSource(corrId, repository.ownerId, it, repository) })
      }
      it.update?.let {
        val sources: List<SourceEntity> = it.mapNotNull{ scrapeRequestUpdate -> run {
            val source = repository.sources.firstOrNull { it.id.toString() == scrapeRequestUpdate.where.id }
            source?.tags = scrapeRequestUpdate.data.tags.set.toTypedArray()
            source
          }
        }

        sourceDAO.saveAll(sources)
      }
      it.remove?.let {
        val deleteIds = it.map { UUID.fromString(it) }
        sourceDAO.deleteAllById(deleteIds.filter { id -> repository.sources.any { it.id == id } }.toMutableList())
        repository.sources = repository.sources.filter { source -> deleteIds.none { it == source.id } }.toMutableList()
      }
    }

    return repositoryDAO.save(repository)
  }

  fun countAll(userId: UUID?, product: ProductCategory): Int {
    return userId
      ?.let { repositoryDAO.countAllByOwnerIdAndProduct(it, product) }
      ?: repositoryDAO.countAllByVisibility(EntityVisibility.isPublic)
  }
}

private fun ScrapeAction.fromDto(): BrowserActionEntity {
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
  return PluginExecution(id = pluginId, params = params)
}

private fun DocumentEntity.toRichArticle(propertyService: PropertyService, visibility: EntityVisibility): RichArticle {
  val article = RichArticle()
  article.id = id.toString()
  article.title = StringUtils.trimToEmpty(contentTitle)
  article.attachments = attachments.map {
    val a = JsonAttachment()
    a.url = it.remoteDataUrl ?: createAttachmentUrl(propertyService, it.id)
    a.type = it.contentType
    a.size = it.size
    a.duration = it.duration
    a
  }
  if (visibility === EntityVisibility.isPublic) {
    article.url = createDocumentUrl(propertyService, id)
    article.contentText = StringUtils.trimToEmpty(contentText).substring(0..160) + "..."
  } else {
    article.url = url
    article.contentText = StringUtils.trimToEmpty(contentText)
    article.contentRawBase64 = contentRaw?.let { Base64.getEncoder().encodeToString(contentRaw) }
    article.contentRawMime = contentRawMime
    article.contentHtml = contentHtml
  }
  article.publishedAt = publishedAt
  article.modifiedAt = updatedAt
  article.tags = (tags?.asList() ?: emptyList()).plus(getAttachmentTags(article))
  article.startingAt = startingAt
  article.imageUrl = imageUrl
  return article

}

private fun getAttachmentTags(article: RichArticle): List<String> {
  return addListenableTag(article.attachments.filter { it.type.startsWith("audio/") && it.duration != null }
    .map { classifyDuration(it.duration!!) }
    .distinct()
  )
}

fun addListenableTag(tags: List<String>): List<String> {
  return if (tags.isEmpty()) {
    tags
  } else {
    tags.plus("listenable")
  }
}

fun classifyDuration(duration: Long): String {
  return when(duration.div(60.0)) {
    in 0.0..1.0 -> "brief"
    in 1.0 .. 5.0 -> "short"
    in 5.0 .. 30.0 -> "medium"
    else -> "long"
  }}
