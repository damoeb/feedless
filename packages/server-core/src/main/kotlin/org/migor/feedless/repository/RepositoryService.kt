package org.migor.feedless.repository

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.lang3.BooleanUtils
import org.apache.commons.lang3.StringUtils
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.BadRequestException
import org.migor.feedless.EntityVisibility
import org.migor.feedless.NotFoundException
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.Vertical
import org.migor.feedless.actions.PluginExecutionJson
import org.migor.feedless.api.createDocumentUrl
import org.migor.feedless.api.fromDto
import org.migor.feedless.common.PropertyService
import org.migor.feedless.config.CacheNames
import org.migor.feedless.document.Document
import org.migor.feedless.document.DocumentId
import org.migor.feedless.document.DocumentService
import org.migor.feedless.document.ReleaseStatus
import org.migor.feedless.feed.parser.json.JsonAttachment
import org.migor.feedless.feed.parser.json.JsonFeed
import org.migor.feedless.feed.parser.json.JsonItem
import org.migor.feedless.feed.parser.json.JsonPoint
import org.migor.feedless.generated.types.PluginExecutionInput
import org.migor.feedless.generated.types.PluginExecutionParamsInput
import org.migor.feedless.generated.types.RecordOrderByInput
import org.migor.feedless.generated.types.RecordsWhereInput
import org.migor.feedless.generated.types.RepositoryCreateInput
import org.migor.feedless.generated.types.RepositoryUpdateDataInput
import org.migor.feedless.pipeline.plugins.createAttachmentUrl
import org.migor.feedless.pipelineJob.PluginExecution
import org.migor.feedless.plan.PlanConstraintsService
import org.migor.feedless.session.SessionService
import org.migor.feedless.source.SourceId
import org.migor.feedless.source.SourceService
import org.migor.feedless.user.User
import org.migor.feedless.user.UserId
import org.migor.feedless.user.UserService
import org.migor.feedless.user.corrId
import org.migor.feedless.user.userId
import org.migor.feedless.user.userIdOptional
import org.migor.feedless.util.CryptUtil.newCorrId
import org.migor.feedless.util.toLocalDateTime
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Profile
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.util.UriComponentsBuilder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.util.*
import kotlin.coroutines.coroutineContext

fun toPageRequest(page: Int?, pageSize: Int?): Pageable {
  val fixedPage = (page ?: 0).coerceAtLeast(0)
  val fixedPageSize = (pageSize ?: 0).coerceAtLeast(0).coerceAtMost(20)
  return PageRequest.of(fixedPage, fixedPageSize)
}

fun Pageable.toPageableRequest(): org.migor.feedless.PageableRequest {
  return org.migor.feedless.PageableRequest(
    pageNumber = pageNumber,
    pageSize = pageSize,
    sortBy = sort.map { org.migor.feedless.SortableRequest(it.property, it.isAscending) }.toList()
  )
}

@Service
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.repository} & ${AppLayer.service}")
class RepositoryService(
//  private var userDAO: UserDAO,
  private var repositoryDAO: RepositoryRepository,
  private var sessionService: SessionService,
  private var userService: UserService,
  private var planConstraintsService: PlanConstraintsService,
  private var documentService: DocumentService,
  private var propertyService: PropertyService,
  private var sourceService: SourceService,
  private var context: ApplicationContext,
) {

  private val log = LoggerFactory.getLogger(RepositoryService::class.simpleName)

//  @Autowired
//  private lateinit var mailForwardDAO: MailForwardDAO

//  @Autowired
//  private lateinit var analyticsService: AnalyticsService


  @Transactional
  suspend fun create(data: List<RepositoryCreateInput>): List<Repository> {
    log.info("[${coroutineContext.corrId()}] create repository with ${data.size} sources")

    val ownerId = getActualUserOrDefaultUser().id
    val totalCount = repositoryDAO.countByOwnerId(ownerId)
    planConstraintsService.auditRepositoryMaxCount(totalCount, ownerId)
    if (planConstraintsService.violatesRepositoriesMaxActiveCount(ownerId)) {
      log.info("[${coroutineContext.corrId()}] violates maxActiveCount")
      throw IllegalArgumentException("Too many active repositories")
//      log.info("[$corrId] violates maxActiveCount, archiving oldest")
//      RepositoryDAO.updateArchivedForOldestActive(ownerId)
    }
    return data.map { createRepository(ownerId, it) }
  }

  @Cacheable(value = [CacheNames.FEED_SHORT_TTL], key = "\"repo/\" + #repositoryId + #tags")
  suspend fun getFeedByRepositoryId(
    repositoryId: RepositoryId,
    page: Int,
    tags: List<String>,
    where: RecordsWhereInput?,
    orderBy: RecordOrderByInput?,
  ): JsonFeed {
    val repository = context.getBean(RepositoryService::class.java).findById(repositoryId)
      ?: throw IllegalArgumentException("Repository not found")

    val pageSize = 11
    val pageable = toPageRequest(page, pageSize)
    val items = try {
      documentService.findAllByRepositoryId(
        repositoryId,
        status = ReleaseStatus.released,
        tags = tags,
        where = where,
        orderBy = orderBy,
        pageable = pageable,
      ).map { it.toJsonItem(propertyService, repository.visibility) }.toList()

    } catch (e: EmptyResultDataAccessException) {
      val corrId = coroutineContext.corrId()
      log.error("[$corrId] empty result", e)
      emptyList()
    }

    val title = if (repository.visibility === EntityVisibility.isPublic) {
      repository.title
    } else {
      "${repository.title} (Personal use)"
    }

    val jsonFeed = JsonFeed()
    jsonFeed.id = "repository:${repositoryId}"
//    jsonFeed.tags = tags
    jsonFeed.title = title
    jsonFeed.description = repository.description
    jsonFeed.websiteUrl = "${propertyService.appHost}/feeds/$repositoryId"
    jsonFeed.publishedAt = items.maxOfOrNull { it.publishedAt } ?: LocalDateTime.now()
    jsonFeed.items = items.filterIndexed { index, _ -> index < pageSize - 1 }
    jsonFeed.imageUrl = null
    jsonFeed.page = page
    jsonFeed.expired = false
    val urlBuilder = UriComponentsBuilder.fromHttpUrl("${propertyService.apiGatewayUrl}/f/${repositoryId}/atom")
//    if (shareKey != null) {
//      urlBuilder.queryParam("skey", shareKey)
//    }
    jsonFeed.feedUrl = urlBuilder.build().toUri().toString()
    jsonFeed.isLast = items.size < pageSize

    return jsonFeed
  }

  @Transactional(readOnly = true)
  suspend fun findAll(
    pageable: Pageable,
    where: RepositoriesWhereInput?,
    userId: UserId?
  ): List<Repository> {
    log.debug("userId=$userId")
    return repositoryDAO.findAll(pageable.toPageableRequest(), where, userId)
  }

  @Transactional(readOnly = true)
  suspend fun findById(repositoryId: RepositoryId): Repository? {
    return repositoryDAO.findById(repositoryId)
  }

//  @Transactional(readOnly = true)
//  suspend fun findByIdWithSources(repositoryId: UUID, shareKey: String? = null): RepositoryEntity {
//    val sub = withContext(Dispatchers.IO) {
//      repositoryDAO.findById(repositoryId).orElseThrow()
//        ?: throw NotFoundException("Repository $repositoryId not found")
//    }
//    return if (sub.visibility === EntityVisibility.isPublic) {
//      sub
//    } else {
////      if (sub.ownerId == getActualUserOrDefaultUser(corrId).id) {
//      sub
////      } else {
////        if (StringUtils.isNotBlank(sub.shareKey) && sub.shareKey == shareKey) {
////          sub
////        } else {
////          throw PermissionDeniedException("unauthorized ($corrId)")
////        }
////      }
//    }
//  }

  @Transactional
  suspend fun delete(repositoryId: RepositoryId) {
    val repository = repositoryDAO.findById(repositoryId)!!
    if (repository.ownerId != coroutineContext.userId()) {
      throw PermissionDeniedException("not authorized")
    }
    log.info("[${coroutineContext.corrId()}] removing repository $repositoryId")
    repositoryDAO.delete(repository)
  }

  suspend fun calculateScheduledNextAt(
    cron: String,
    ownerId: UserId,
    product: Vertical,
    after: LocalDateTime
  ): LocalDateTime {
    return planConstraintsService.coerceMinScheduledNextAt(
      LocalDateTime.now(),
      nextCronDate(cron, after),
      ownerId,
      product
    )
  }

  suspend fun updateRepository(id: RepositoryId, data: RepositoryUpdateDataInput) {
    // Fetch entity for mutation
    val existingRepository = repositoryDAO.findById(id)
      ?: throw NotFoundException("Repository not found")
    
    var repository = existingRepository.copy(
      lastUpdatedAt = LocalDateTime.now()
    )

    val corrId = coroutineContext.corrId()
    if (repository.ownerId != coroutineContext.userId()) {
      throw PermissionDeniedException("not authorized")
    }
    log.info("[$corrId] update $id")
    repository = data.title?.set?.let { repository.copy(title = it) } ?: repository
    repository = data.description?.set?.let { repository.copy(description = it) } ?: repository


    val product = sessionService.activeProductFromRequest()!!
    repository = data.refreshCron?.let {
      it.set?.let {
        repository.copy(
          sourcesSyncCron = planConstraintsService.auditCronExpression(it),
          triggerScheduledNextAt = calculateScheduledNextAt(
            it, repository.ownerId,
            product,
            repository.lastUpdatedAt
          )
        )
      }
    } ?: repository

    repository = data.pushNotificationsMuted?.let {
      repository.copy(pushNotificationsEnabled = it.set)
    } ?: repository

    repository =
      data.visibility?.set?.let { repository.copy(visibility = planConstraintsService.coerceVisibility(it.fromDto())) }
        ?: repository
    repository = data.plugins?.let { plugins ->
      val newPlugins = plugins.map { it.fromDto() }.sortedBy { it.id }.toMutableList()
      if (newPlugins != repository.plugins) {
        log.info("[${coroutineContext.corrId()}] plugins $newPlugins")
        repository.copy(plugins = newPlugins)
      } else {
        repository
      }
    } ?: repository

    repository = data.nextUpdateAt?.let {
      val next = it.set?.toLocalDateTime() ?: LocalDateTime.now()
      val nextAt = planConstraintsService.coerceMinScheduledNextAt(
        repository.lastUpdatedAt,
        next,
        repository.ownerId,
        product
      )
      log.info("[${coroutineContext.corrId()}] nextUpdateAt $nextAt")
      repository.copy(triggerScheduledNextAt = nextAt)
    } ?: repository

    repository = data.retention?.let { retention ->
      var updated = repository
      retention.maxAgeDays?.let {
        log.info("[${coroutineContext.corrId()}] retentionMaxAgeDays ${it.set}")
        updated = updated.copy(retentionMaxAgeDays = it.set)
      }
      retention.maxCapacity?.let {
        log.info("[${coroutineContext.corrId()}] retentionMaxItems ${it.set}")
        updated = updated.copy(retentionMaxCapacity = it.set)
      }
      if (retention.maxAgeDays != null || retention.maxCapacity != null) {
        documentService.applyRetentionStrategy(repository.id)
      }
      updated
    } ?: repository

    data.sources?.let {
      it.add?.let {
        sourceService.createSources(repository.ownerId, it, repository.id)
      }
      it.update?.let {
        sourceService.updateSources(repository.id, it)
      }
      it.remove?.let {
        sourceService.deleteAllById(repository.id, it.map { SourceId(it) })
      }
    }
    withContext(Dispatchers.IO) {
      repositoryDAO.save(repository)
    }
  }

  @Transactional(readOnly = true)
  suspend fun countAll(userId: UserId?, product: Vertical): Int {
    return userId
      ?.let { repositoryDAO.countAllByOwnerIdAndProduct(it, product) }
      ?: repositoryDAO.countAllByVisibility(EntityVisibility.isPublic)
  }

  @Transactional
  suspend fun updatePullsFromAnalytics(repositoryId: RepositoryId, pulls: Int) {
    val repository = repositoryDAO.findById(repositoryId)!!
    repositoryDAO.save(
      repository.copy(
        pullsPerMonth = pulls,
        lastUpdatedAt = LocalDateTime.now()
      )
    )
  }

  private suspend fun getActualUserOrDefaultUser(): User {
    return coroutineContext.userIdOptional()?.let {
      sessionService.user()
    } ?: userService.getAnonymousUser()
      .also { log.debug("[${coroutineContext.corrId()}] fallback to user anonymous") }
  }

  private suspend fun createRepository(
    ownerId: UserId,
    repoInput: RepositoryCreateInput
  ): Repository {

//    val product = sessionService.activeProductFromRequest()!!
    val product = repoInput.product.fromDto()
    var repo = Repository(
      shareKey = newCorrId(10),
      title = repoInput.title,
      description = repoInput.description,
      visibility = planConstraintsService.coerceVisibility(repoInput.visibility?.fromDto()),
      ownerId = ownerId,
      pushNotificationsEnabled = BooleanUtils.isTrue(repoInput.pushNotificationsMuted),
      retentionMaxCapacity =
        planConstraintsService.coerceRetentionMaxCapacity(repoInput.retention?.maxCapacity, ownerId, product),
      retentionMaxAgeDays = planConstraintsService.coerceRetentionMaxAgeDays(
        repoInput.retention?.maxAgeDays,
        ownerId,
        product
      ),
      product = product,
      sourcesSyncCron = repoInput.refreshCron?.let {
        planConstraintsService.auditCronExpression(repoInput.refreshCron ?: "")
      } ?: ""
    )


    planConstraintsService.auditSourcesMaxCountPerRepository(repoInput.sources.size, ownerId, product)

    repo = repoInput.plugins?.let {
      if (it.size > 5) {
        throw BadRequestException("Too many plugins ${it.size}, limit 5")
      }
      repo.copy(plugins = it.map { plugin -> plugin.fromDto() })
    } ?: repo

//    if (subInput.withShareKey) {
//      newCorrId(10)
//    } else {
//      ""
//    }

    val saved = repositoryDAO.save(repo)

    sourceService.createSources(ownerId, repoInput.sources, repo.id)


//    repoInput.additionalSinks?.let { sink ->
//      val owner = withContext(Dispatchers.IO) {
//        userDAO.findById(ownerId).orElseThrow()
//      }
//      repo.mailForwards = sink.mapNotNull { it.email }
//        .map { createMailForwarder(corrId, it, repo, owner, repo.product) }
//        .toMutableList()
//    }

    return saved
  }

//  private suspend fun createMailForwarder(
//    corrId: String,
//    email: String,
//    sub: RepositoryEntity,
//    owner: UserEntity,
//    product: ProductCategory
//  ): MailForwardEntity {
//    val forward = MailForwardEntity()
//    forward.email = email
//    forward.authorized = email == owner.email
//    forward.repositoryId = sub.id
//
//    return withContext(Dispatchers.IO) {
//      mailForwardDAO.save(forward)
//    }
//  }

  @Transactional
  suspend fun findBySourceId(sourceId: SourceId): Repository? {
    return repositoryDAO.findBySourceId(sourceId)
  }

  @Transactional(readOnly = true)
  suspend fun findAllByVisibilityAndLastPullSyncBefore(
    visibility: EntityVisibility,
    now: LocalDateTime,
    pageable: PageRequest
  ): List<Repository> {
    return repositoryDAO.findAllByVisibilityAndLastPullSyncBefore(visibility, now, pageable.toPageableRequest())
  }

  @Transactional(readOnly = true)
  suspend fun findAllWhereNextHarvestIsDue(now: LocalDateTime, pageable: PageRequest): List<Repository> {
    return repositoryDAO.findAllWhereNextHarvestIsDue(now, pageable.toPageableRequest())
  }

  @Transactional(readOnly = true)
  suspend fun findByDocumentId(documentId: DocumentId): Repository? {
    return repositoryDAO.findByDocumentId(documentId)
  }

  @Transactional
  suspend fun save(repository: Repository): Repository {
    return repositoryDAO.save(repository)
  }

  @Transactional(readOnly = true)
  suspend fun findByTitleAndOwnerId(title: String, ownerId: UserId): Repository? {
    return repositoryDAO.findByTitleAndOwnerId(title, ownerId)
  }
}

fun PluginExecutionInput.fromDto(): PluginExecution {
  return PluginExecution(id = pluginId, params = params.toEntity())
}

fun PluginExecutionParamsInput.toEntity(): PluginExecutionJson {
  val data = listOfNotNull(
    org_feedless_filter,
    org_feedless_feed,
    org_feedless_diff_records,
    jsonData,
    org_feedless_conditional_tag,
    org_feedless_fulltext
  )
    .firstOrNull()

  return PluginExecutionJson(
    paramsJsonString = data?.let { Gson().toJson(it) },
  )
}

fun Document.toJsonItem(
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
  article.title = StringUtils.trimToEmpty(title)
  article.attachments = attachments.map {
    JsonAttachment(
      url = it.remoteDataUrl ?: createAttachmentUrl(propertyService, it.id),
      type = it.mimeType,
      length = it.size,
      duration = it.duration
    )
  }
  if (visibility === EntityVisibility.isPublic) {
    article.url = createDocumentUrl(propertyService, id)
    article.text = StringUtils.abbreviate(text, "...", 160)
  } else {
    article.url = url
    article.text = StringUtils.trimToEmpty(text)
    article.rawBase64 = raw?.let { Base64.getEncoder().encodeToString(raw) }
    article.rawMimeType = rawMimeType
    article.html = html
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
