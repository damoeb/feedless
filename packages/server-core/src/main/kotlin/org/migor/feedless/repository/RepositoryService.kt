package org.migor.feedless.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.lang3.StringUtils
import org.migor.feedless.AppProfiles
import org.migor.feedless.BadRequestException
import org.migor.feedless.NotFoundException
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.actions.ScrapeActionDAO
import org.migor.feedless.api.fromDto
import org.migor.feedless.attachment.createAttachmentUrl
import org.migor.feedless.common.PropertyService
import org.migor.feedless.config.CacheNames
import org.migor.feedless.data.jpa.enums.EntityVisibility
import org.migor.feedless.data.jpa.enums.ProductCategory
import org.migor.feedless.data.jpa.enums.ReleaseStatus
import org.migor.feedless.data.jpa.enums.fromDto
import org.migor.feedless.document.DocumentEntity
import org.migor.feedless.document.DocumentService
import org.migor.feedless.document.createDocumentUrl
import org.migor.feedless.feed.parser.json.JsonAttachment
import org.migor.feedless.feed.parser.json.JsonFeed
import org.migor.feedless.feed.parser.json.JsonItem
import org.migor.feedless.feed.parser.json.JsonPoint
import org.migor.feedless.generated.types.PluginExecutionInput
import org.migor.feedless.generated.types.RepositoriesCreateInput
import org.migor.feedless.generated.types.RepositoriesWhereInput
import org.migor.feedless.generated.types.Repository
import org.migor.feedless.generated.types.RepositoryCreateInput
import org.migor.feedless.generated.types.RepositoryUpdateDataInput
import org.migor.feedless.generated.types.SourceInput
import org.migor.feedless.generated.types.Visibility
import org.migor.feedless.mail.MailForwardDAO
import org.migor.feedless.mail.MailForwardEntity
import org.migor.feedless.plan.PlanConstraintsService
import org.migor.feedless.session.SessionService
import org.migor.feedless.source.SourceDAO
import org.migor.feedless.source.SourceEntity
import org.migor.feedless.user.UserDAO
import org.migor.feedless.user.UserEntity
import org.migor.feedless.user.UserService
import org.migor.feedless.util.CryptUtil.newCorrId
import org.migor.feedless.util.JtsUtil
import org.migor.feedless.util.toLocalDateTime
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.annotation.Profile
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.util.UriComponentsBuilder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.util.*

fun toPageRequest(page: Int?, pageSize: Int?): Pageable {
  val fixedPage = (page ?: 0).coerceAtLeast(0)
  val fixedPageSize = (pageSize ?: 0).coerceAtLeast(1).coerceAtMost(20)
  return PageRequest.of(fixedPage, fixedPageSize)
}


@Service
@Profile(AppProfiles.database)
@Transactional
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

  @Autowired
  private lateinit var sessionService: SessionService

  @Autowired
  private lateinit var userService: UserService

  @Autowired
  private lateinit var planConstraintsService: PlanConstraintsService

  @Autowired
  private lateinit var documentService: DocumentService

  @Autowired
  private lateinit var propertyService: PropertyService

  @Autowired
  private lateinit var scrapeActionDAO: ScrapeActionDAO

  suspend fun create(corrId: String, data: RepositoriesCreateInput): List<Repository> {
    log.info("[$corrId] create repository with ${data.repositories.size} sources")

    val ownerId = getActualUserOrDefaultUser(corrId).id
    val totalCount = withContext(Dispatchers.IO) {
      repositoryDAO.countByOwnerId(ownerId)
    }
    planConstraintsService.auditRepositoryMaxCount(totalCount, ownerId)
    if (planConstraintsService.violatesRepositoriesMaxActiveCount(ownerId)) {
      log.info("[$corrId] violates maxActiveCount")
      throw IllegalArgumentException("Too many active repositories")
//      log.info("[$corrId] violates maxActiveCount, archiving oldest")
//      RepositoryDAO.updateArchivedForOldestActive(ownerId)
    }
    return data.repositories.map { createRepository(corrId, ownerId, it).toDto() }
  }

  private suspend fun getActualUserOrDefaultUser(corrId: String): UserEntity {
    return sessionService.userId()?.let {
      sessionService.user(corrId)
    } ?: userService.getAnonymousUser().also { log.debug("[$corrId] fallback to user anonymous") }
  }

  private suspend fun createRepository(
    corrId: String,
    ownerId: UUID,
    repoInput: RepositoryCreateInput
  ): RepositoryEntity {
    val repo = RepositoryEntity()

    repo.shareKey = newCorrId(10)
    repo.title = repoInput.sinkOptions.title
    repo.description = repoInput.sinkOptions.description
    repo.visibility = planConstraintsService.coerceVisibility(corrId, repoInput.sinkOptions.visibility?.fromDto())
    val product = sessionService.activeProductFromRequest()!!

    planConstraintsService.auditSourcesMaxCountPerRepository(repoInput.sources.size, ownerId, product)
    repo.ownerId = ownerId
    repoInput.sinkOptions.plugins?.let {
      if (it.size > 5) {
        throw BadRequestException("Too many plugins ${it.size}, limit 5")
      }
      repo.plugins = it.map { plugin -> plugin.fromDto() }
    }
    repo.shareKey = newCorrId(10)
//    if (subInput.sinkOptions.withShareKey) {
//      newCorrId(10)
//    } else {
//      ""
//    }

    repo.sourcesSyncCron = repoInput.sinkOptions.refreshCron?.let {
      planConstraintsService.auditCronExpression(repoInput.sinkOptions.refreshCron)
    } ?: ""
    repo.retentionMaxCapacity =
      planConstraintsService.coerceRetentionMaxCapacity(repoInput.sinkOptions.retention?.maxCapacity, ownerId, product)
    repo.retentionMaxAgeDays = planConstraintsService.coerceRetentionMaxAgeDays(
      repoInput.sinkOptions.retention?.maxAgeDays,
      ownerId,
      product
    )
    repo.product = repoInput.product.fromDto()

    val saved = withContext(Dispatchers.IO) {
      repositoryDAO.save(repo)
    }

    repo.sources = repoInput.sources.map { createScrapeSource(corrId, ownerId, it, repo) }.toMutableList()

    repoInput.additionalSinks?.let { sink ->
      val owner = withContext(Dispatchers.IO) {
        userDAO.findById(ownerId).orElseThrow()
      }
      repo.mailForwards = sink.mapNotNull { it.email }
        .map { createMailForwarder(corrId, it, repo, owner, repo.product) }
        .toMutableList()
    }

    return saved
  }

  private suspend fun createMailForwarder(
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

    return withContext(Dispatchers.IO) {
      mailForwardDAO.save(forward)
    }
  }

  private suspend fun createScrapeSource(
    corrId: String,
    ownerId: UUID,
    req: SourceInput,
    repository: RepositoryEntity
  ): SourceEntity {
    val entity = SourceEntity()
    log.info("[$corrId] create source")
    val source = req.fromDto()
    planConstraintsService.auditScrapeRequestMaxActions(source.actions.size, ownerId)
//    planConstraintsService.auditScrapeRequestTimeout(scrapeRequest.page.timeout, ownerId)
    entity.tags = source.tags
    entity.title = req.title
    entity.repositoryId = repository.id
    req.localized?.let {
      entity.latLon = JtsUtil.createPoint(it.lat, it.lon)
    } ?: run {
      entity.latLon = null
    }

    val saved = withContext(Dispatchers.IO) {
      sourceDAO.save(entity)
    }

    source.actions.forEachIndexed { index, scrapeAction ->
      run {
        scrapeAction.sourceId = entity.id
        scrapeAction.pos = index
      }
    }
    withContext(Dispatchers.IO) {
      scrapeActionDAO.saveAll(source.actions)
    }
    return saved
  }

  @Cacheable(value = [CacheNames.FEED_SHORT_TTL], key = "\"repo/\" + #repositoryId + #tag")
  suspend fun getFeedByRepositoryId(
    corrId: String,
    repositoryId: String,
    page: Int,
    tag: String? = null,
    shareKey: String? = null
  ): JsonFeed {
    val id = UUID.fromString(repositoryId)
    val repository = findById(corrId, id, shareKey)

    val pageSize = 11
    val pageable = toPageRequest(page, pageSize)
    val items = try {
      val pageResult = withContext(Dispatchers.IO) {
        documentService.findAllByRepositoryId(
          id,
          status = ReleaseStatus.released,
          tag = tag,
          pageable = pageable,
          shareKey = shareKey
        )
      }
       pageResult.mapNotNull { it?.toJsonItem(propertyService, repository.visibility) }.toList()

    } catch (e: EmptyResultDataAccessException) {
      log.error("[$corrId] empty result", e)
      emptyList()
    }

    val tags = repository.sources.mapNotNull { it.tags?.asList() }.flatten().distinct()

    val title = if (repository.visibility === EntityVisibility.isPublic) {
      repository.title
    } else {
      "${repository.title} (Personal use)"
    }

    val jsonFeed = JsonFeed()
    jsonFeed.id = "repository:${repositoryId}"
    jsonFeed.tags = tags
    jsonFeed.title = title
    jsonFeed.description = repository.description
    jsonFeed.websiteUrl = "${propertyService.appHost}/feeds/$repositoryId"
    jsonFeed.publishedAt = items.maxOfOrNull { it.publishedAt } ?: LocalDateTime.now()
    jsonFeed.items = items.filterIndexed { index, _ -> index < pageSize - 1 }
    jsonFeed.imageUrl = null
    jsonFeed.page = page
    jsonFeed.expired = false
    val urlBuilder = UriComponentsBuilder.fromHttpUrl("${propertyService.apiGatewayUrl}/f/${repositoryId}/atom")
    if (shareKey != null) {
      urlBuilder.queryParam("skey", shareKey)
    }
    jsonFeed.feedUrl = urlBuilder.build().toUri().toString()
    jsonFeed.isLast = items.size < pageSize

    return jsonFeed
  }

  suspend fun findAll(
    offset: Int,
    pageSize: Int,
    where: RepositoriesWhereInput?,
    userId: UUID?
  ): List<RepositoryEntity> {
    val pageable =
      PageRequest.of(offset, pageSize.coerceAtMost(10), Sort.by(Sort.Direction.DESC, "createdAt"))
    log.debug("userId=$userId")
    return withContext(Dispatchers.IO) {
      userId
        ?.let { repositoryDAO.findAllByOwnerId(it, pageable) }
        ?: repositoryDAO.findAllByVisibility(EntityVisibility.isPublic, pageable)
    }
  }

  suspend fun findById(corrId: String, repositoryId: UUID, shareKey: String? = null): RepositoryEntity {
    val sub = withContext(Dispatchers.IO) {
      repositoryDAO.findByIdWithSources(repositoryId) ?: throw NotFoundException("Repository $repositoryId not found ($corrId)")
    }
    return if (sub.visibility === EntityVisibility.isPublic) {
      sub
    } else {
//      if (sub.ownerId == getActualUserOrDefaultUser(corrId).id) {
        sub
//      } else {
//        if (StringUtils.isNotBlank(sub.shareKey) && sub.shareKey == shareKey) {
//          sub
//        } else {
//          throw PermissionDeniedException("unauthorized ($corrId)")
//        }
//      }
    }
  }

  suspend fun delete(corrId: String, id: UUID) {
    withContext(Dispatchers.IO) {
      val sub = repositoryDAO.findById(id).orElseThrow()
      if (sub.ownerId != sessionService.userId()) {
        throw PermissionDeniedException("not authorized")
      }
      repositoryDAO.delete(sub)
    }
  }

  suspend fun calculateScheduledNextAt(
    cron: String,
    ownerId: UUID,
    product: ProductCategory,
    after: LocalDateTime
  ): LocalDateTime {
    return planConstraintsService.coerceMinScheduledNextAt(
      LocalDateTime.now(),
      nextCronDate(cron, after),
      ownerId,
      product
    )
  }

  suspend fun update(corrId: String, id: UUID, data: RepositoryUpdateDataInput): RepositoryEntity {
    val repository = withContext(Dispatchers.IO) {
      repositoryDAO.findByIdWithSources(id) ?: throw NotFoundException("Repository $id not found")
    }
    if (repository.ownerId != sessionService.userId()) {
      throw PermissionDeniedException("not authorized")
    }
    data.title?.set?.let { repository.title = it }
    data.description?.set?.let { repository.description = it }
    val product = sessionService.activeProductFromRequest()!!
    data.refreshCron?.let {
      it.set?.let {
        repository.sourcesSyncCron = planConstraintsService.auditCronExpression(it)
        repository.triggerScheduledNextAt = calculateScheduledNextAt(
          it, repository.ownerId,
          product,
          repository.lastUpdatedAt
        )
      }
    }
    data.visibility?.set?.let { repository.visibility = planConstraintsService.coerceVisibility(corrId, it.fromDto()) }
    data.plugins?.let { plugins ->
      val newPlugins = plugins.map { it.fromDto() }.sortedBy { it.id }.toMutableList()
      if (newPlugins != repository.plugins) {
        repository.plugins = newPlugins
        log.info("[$corrId] plugins ${newPlugins}")
      }
    }
    data.nextUpdateAt?.let {
      val next = it.set?.toLocalDateTime() ?: LocalDateTime.now()
      repository.triggerScheduledNextAt = planConstraintsService.coerceMinScheduledNextAt(
        repository.lastUpdatedAt,
        next,
        repository.ownerId,
        product
      )
      log.info("[$corrId] nextUpdateAt ${repository.triggerScheduledNextAt}")
    }
    data.retention?.let {
      it.maxAgeDays?.let {
        repository.retentionMaxAgeDays = it.set
        log.info("[$corrId] retentionMaxAgeDays ${it.set}")
      }
      it.maxCapacity?.let {
        repository.retentionMaxCapacity = it.set
        log.info("[$corrId] retentionMaxItems ${it.set}")
      }
      if (it.maxAgeDays != null || it.maxCapacity != null) {
        documentService.applyRetentionStrategy(corrId, repository.id)
      }
    }
    return withContext(Dispatchers.IO) {
      data.sources?.let {
        it.add?.let {
          repository.sources.addAll(it.map { createScrapeSource(corrId, repository.ownerId, it, repository) })
        }
        it.update?.let {
          val sources: List<SourceEntity> = it.mapNotNull { sourceUpdate ->
            run {
              val source = repository.sources.firstOrNull { it.id.toString() == sourceUpdate.where.id }
              sourceUpdate.data.tags?.let {
                source?.tags = sourceUpdate.data.tags.set.toTypedArray()
              }
              sourceUpdate.data.localized?.let { point ->
                point.set?.let {
                  source?.latLon = JtsUtil.createPoint(it.lat, it.lon)
                } ?: run { source?.latLon = null }
              }
              sourceUpdate.data.disabled?.let { disabled ->
                source?.disabled = disabled.set
                source?.errorsInSuccession = 0
              }

              source
            }
          }

          sourceDAO.saveAll(sources)
        }
        it.remove?.let {
          val deleteIds = it.map { UUID.fromString(it) }
          sourceDAO.deleteAllById(deleteIds.filter { id -> repository.sources.any { it.id == id } }.toMutableList())
          repository.sources =
            repository.sources.filter { source -> deleteIds.none { it == source.id } }.toMutableList()
        }
      }
      repositoryDAO.save(repository)
    }
  }

  fun countAll(userId: UUID?, product: ProductCategory): Int {
    return userId
      ?.let { repositoryDAO.countAllByOwnerIdAndProduct(it, product) }
      ?: repositoryDAO.countAllByVisibility(EntityVisibility.isPublic)
  }

  fun getRepoTitleForFeedlessOpsNotifications(): String = "feedlessOpsNotifications"
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

fun DocumentEntity.toJsonItem(
  propertyService: PropertyService,
  visibility: EntityVisibility,
  requestURI: String? = null
): JsonItem {
  val article = JsonItem()
  article.id = id.toString()
  latLon?.let {
    val point = JsonPoint()
    point.x = it.x
    point.y = it.y
    article.latLng = point
  }
  article.title = StringUtils.trimToEmpty(contentTitle)
  article.attachments = attachments.map {
    JsonAttachment(
      url = it.remoteDataUrl ?: createAttachmentUrl(propertyService, it.id),
      type = it.contentType,
      length = it.size,
      duration = it.duration
    )
  }
  if (visibility === EntityVisibility.isPublic) {
    article.url = createDocumentUrl(propertyService, id)
    article.contentText = StringUtils.abbreviate(contentText, "...", 160)
  } else {
    article.url = url
    article.contentText = StringUtils.trimToEmpty(contentText)
    article.contentRawBase64 = contentRaw?.let { Base64.getEncoder().encodeToString(contentRaw) }
    article.contentRawMime = contentRawMime
    article.contentHtml = contentHtml
  }
  requestURI?.let {
    article.url += "?source=${URLEncoder.encode(requestURI, StandardCharsets.UTF_8)}"
  }

  article.publishedAt = publishedAt
  article.modifiedAt = updatedAt
  article.tags = (tags?.asList() ?: emptyList()).plus(getAttachmentTags(article))
  article.startingAt = startingAt
  article.imageUrl = imageUrl
  return article

}

private fun getAttachmentTags(article: JsonItem): List<String> {
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
  return when (duration.div(60.0)) {
    in 0.0..1.0 -> "brief"
    in 1.0..5.0 -> "short"
    in 5.0..30.0 -> "medium"
    else -> "long"
  }
}
