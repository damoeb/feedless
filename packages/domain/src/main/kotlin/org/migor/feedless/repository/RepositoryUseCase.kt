package org.migor.feedless.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import org.apache.commons.lang3.StringUtils
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.BadRequestException
import org.migor.feedless.EntityVisibility
import org.migor.feedless.PageableRequest
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.Vertical
import org.migor.feedless.api.createDocumentUrl
import org.migor.feedless.capability.CapabilityId
import org.migor.feedless.capability.UnresolvedCapability
import org.migor.feedless.common.AppConfig
import org.migor.feedless.config.CacheNames
import org.migor.feedless.document.Document
import org.migor.feedless.document.DocumentUseCase
import org.migor.feedless.document.DocumentsFilter
import org.migor.feedless.document.RecordOrderBy
import org.migor.feedless.document.ReleaseStatus
import org.migor.feedless.feed.parser.json.JsonAttachment
import org.migor.feedless.feed.parser.json.JsonFeed
import org.migor.feedless.feed.parser.json.JsonItem
import org.migor.feedless.feed.parser.json.JsonPoint
import org.migor.feedless.group.GroupId
import org.migor.feedless.pipeline.plugins.createAttachmentUrl
import org.migor.feedless.plan.PlanConstraintsService
import org.migor.feedless.source.SourceUseCase
import org.migor.feedless.user.UserId
import org.migor.feedless.user.groupId
import org.migor.feedless.user.userId
import org.migor.feedless.util.CryptUtil
import org.migor.feedless.util.CryptUtil.newCorrId
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.annotation.Profile
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.util.*

fun toPageRequest(page: Int?, pageSize: Int?): Pageable {
  val fixedPage = (page ?: 0).coerceAtLeast(0)
  val fixedPageSize = (pageSize ?: 0).coerceAtLeast(0).coerceAtMost(20)
  return PageRequest.of(fixedPage, fixedPageSize)
}

fun Pageable.toPageableRequest(): PageableRequest {
  return PageableRequest(
    pageNumber = pageNumber,
    pageSize = pageSize,
    sortBy = sort.map { org.migor.feedless.SortableRequest(it.property, it.isAscending) }.toList()
  )
}

@Service
@Profile("${AppProfiles.repository} & ${AppLayer.service}")
class RepositoryUseCase(
  private val repositoryRepository: RepositoryRepository,
  private val planConstraintsService: PlanConstraintsService,
  private val documentUseCase: DocumentUseCase,
  private val appConfig: AppConfig,
  private val sourceUseCase: SourceUseCase,
  private val repositoryGuard: RepositoryGuard,
) : RepositoryProvider {

  private val log = LoggerFactory.getLogger(RepositoryUseCase::class.simpleName)

  suspend fun create(data: List<RepositoryCreate>): List<Repository> =
    withContext(Dispatchers.IO) {
      log.info("create repository with ${data.size} sources")

      val groupId = coroutineContext.groupId()

      val totalCount = repositoryRepository.countByGroupId(groupId)
      planConstraintsService.auditRepositoryMaxCount(totalCount, groupId)
      if (planConstraintsService.violatesRepositoriesMaxActiveCount(groupId)) {
        log.info("violates maxActiveCount")
        throw IllegalArgumentException("Too many active repositories")
//      log.info("violates maxActiveCount, archiving oldest")
//      RepositoryDAO.updateArchivedForOldestActive(ownerId)
      }
      data.map { createRepository(it) }
    }

  // the grant proves the read check ran, so the key holds nothing user- or key-specific
  @Cacheable(
    value = [CacheNames.FEED_SHORT_TTL],
    key = "\"repo/\" + #grant.repositoryId + \"/\" + #page + \"/\" + #filter + \"/\" + #order"
  )
  suspend fun getFeedByRepositoryId(
    grant: RepositoryReadGrant,
    page: Int,
    filter: DocumentsFilter?,
    order: RecordOrderBy?,
  ): JsonFeed {
    val repositoryId = grant.repositoryId
    log.debug("getFeedByRepositoryId repositoryId=$repositoryId page=$page")
    val repository = repositoryRepository.findById(repositoryId)
      ?: throw IllegalArgumentException("Repository not found")

    val pageSize = 11
    val pageable = toPageRequest(page, pageSize).toPageableRequest()
    val items = try {
      documentUseCase.findAllByRepositoryId(
        repositoryId,
        status = ReleaseStatus.released,
        tags = emptyList(),
        filter = filter,
        orderBy = order,
        pageable = pageable,
      ).map { it.toJsonItem(appConfig, repository.visibility) }.toList()

    } catch (e: EmptyResultDataAccessException) {
      log.error("empty result", e)
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
    jsonFeed.websiteUrl = "${appConfig.appHost}/feeds/$repositoryId"
    jsonFeed.publishedAt = items.maxOfOrNull { it.publishedAt } ?: LocalDateTime.now()
    jsonFeed.items = items.filterIndexed { index, _ -> index < pageSize - 1 }
    jsonFeed.imageUrl = null
    jsonFeed.page = page
    jsonFeed.expired = false
    val urlBuilder = UriComponentsBuilder.fromHttpUrl("${appConfig.apiGatewayUrl}/f/${repositoryId}/atom")
    jsonFeed.feedUrl = urlBuilder.build().toUri().toString()
    jsonFeed.isLast = items.size < pageSize

    return jsonFeed
  }

  suspend fun findAllByUserId(
    pageable: PageableRequest,
    where: RepositoriesFilter?,
    userId: UserId?
  ): List<Repository> {
    log.debug("findAllByUserId userId=$userId")
    return repositoryRepository.findAll(pageable, where, userId)
  }

  suspend fun findById(repositoryId: RepositoryId): Repository? = withContext(Dispatchers.IO) {
    log.debug("findById repositoryId=$repositoryId")
    repositoryRepository.findById(repositoryId)
  }

  suspend fun delete(repositoryId: RepositoryId) {
    val repository = repositoryRepository.findById(repositoryId)!!
    if (repository.ownerId != currentCoroutineContext().userId()) {
      throw PermissionDeniedException("not authorized")
    }
    log.info("removing repository $repositoryId")
    repositoryRepository.delete(repository)
  }

  suspend fun calculateScheduledNextAt(
    cron: String,
    groupId: GroupId,
    after: LocalDateTime
  ): LocalDateTime {
    log.debug("calculateScheduledNextAt cron=$cron groupId=$groupId")
    return planConstraintsService.coerceMinScheduledNextAt(
      LocalDateTime.now(),
      nextCronDate(cron, after),
      groupId,
    )
  }

  suspend fun updateRepository(id: RepositoryId, data: RepositoryUpdate) {
    // Fetch entity for mutation
    val existingRepository = repositoryGuard.requireWrite(id)

    var repository = existingRepository.copy(
      lastUpdatedAt = LocalDateTime.now()
    )

    val userId = currentCoroutineContext().userId()
    if (repository.ownerId != userId) {
      throw PermissionDeniedException("not authorized")
    }
    log.info("update $id")
    repository = data.title?.let { repository.copy(title = it) } ?: repository
    repository = data.description?.let { repository.copy(description = it) } ?: repository

    val groupId = currentCoroutineContext().groupId()
    repository = data.refreshCron?.let {
      repository.copy(
        sourcesSyncCron = planConstraintsService.auditCronExpression(it),
        triggerScheduledNextAt = calculateScheduledNextAt(
          it,
          groupId,
          repository.lastUpdatedAt
        )
      )
    } ?: repository

    repository = data.pushNotificationsEnabled?.let {
      repository.copy(pushNotificationsEnabled = it)
    } ?: repository

    repository = data.visibility?.let {
      repository.copy(
        visibility = planConstraintsService.coerceVisibility(
          groupId,
          it
        )
      )
    } ?: repository

    repository = data.plugins?.let { plugins ->
      val newPlugins = plugins.sortedBy { it.id }.toMutableList()
      if (newPlugins != repository.plugins) {
        log.info("plugins $newPlugins")
        repository.copy(plugins = newPlugins)
      } else {
        repository
      }
    } ?: repository

    if (data.nextUpdateAt != null || data.scheduleNextUpdateNow) {
      val next = data.nextUpdateAt ?: LocalDateTime.now()
      val nextAt = planConstraintsService.coerceMinScheduledNextAt(
        repository.lastUpdatedAt,
        next,
        groupId
      )
      log.info("nextUpdateAt $nextAt")
      repository = repository.copy(triggerScheduledNextAt = nextAt)
    }

    var retentionTouched = false
    if (data.clearRetentionMaxAgeDays) {
      log.info("retentionMaxAgeDays null")
      repository = repository.copy(retentionMaxAgeDays = null)
      retentionTouched = true
    } else if (data.retentionMaxAgeDays != null) {
      log.info("retentionMaxAgeDays ${data.retentionMaxAgeDays}")
      repository = repository.copy(retentionMaxAgeDays = data.retentionMaxAgeDays)
      retentionTouched = true
    }
    if (data.clearRetentionMaxCapacity) {
      log.info("retentionMaxItems null")
      repository = repository.copy(retentionMaxCapacity = null)
      retentionTouched = true
    } else if (data.retentionMaxCapacity != null) {
      log.info("retentionMaxItems ${data.retentionMaxCapacity}")
      repository = repository.copy(retentionMaxCapacity = data.retentionMaxCapacity)
      retentionTouched = true
    }
    data.retentionMaxAgeDaysReferenceField?.let {
      log.info("retentionMaxAgeDaysReferenceField $it")
      repository = repository.copy(retentionMaxAgeDaysReferenceField = it)
    }
    if (retentionTouched) {
      documentUseCase.applyRetentionStrategy(repository.id)
    }

    data.sources?.let { sources ->
      sources.add?.let { sourceUseCase.createSources(it, repository.id) }
      sources.update?.let { sourceUseCase.updateSources(repository.id, it) }
      sources.remove?.let { sourceUseCase.deleteAllById(repository.id, it) }
    }
    withContext(Dispatchers.IO) {
      repositoryRepository.save(repository)
    }
  }

  suspend fun countAll(userId: UserId?, product: Vertical): Int {
    log.debug("countAll userId=$userId product=$product")
    return userId
      ?.let { repositoryRepository.countAllByOwnerIdAndProduct(it, product) }
      ?: repositoryRepository.countAllByVisibility(EntityVisibility.isPublic)
  }

  suspend fun countAllByUserId(where: RepositoriesFilter?, userId: UserId?): Int {
    log.debug("countAllByUserId userId=$userId")
    return repositoryRepository.countAllByUserId(where, userId)
  }

  suspend fun updatePullsFromAnalytics(repositoryId: RepositoryId, pulls: Int) {
    log.debug("updatePullsFromAnalytics repositoryId=$repositoryId pulls=$pulls")
    val repository = repositoryRepository.findById(repositoryId)!!
    repositoryRepository.save(
      repository.copy(
        pullsPerMonth = pulls,
        lastUpdatedAt = LocalDateTime.now()
      )
    )
  }

//  private suspend fun getActualUserOrDefaultUser(): User {
//    return userId()?.let {
//      sessionService.user()
//    } ?: userRepository.findByAnonymousUser()
//      .also { log.debug("fallback to user anonymous") }
//  }

  private suspend fun createRepository(
    repoInput: RepositoryCreate
  ): Repository {

    val groupId = currentCoroutineContext().groupId()
    var repo = Repository(
      shareKey = CryptUtil.newShareKey(),
      title = repoInput.title,
      description = repoInput.description,
      visibility = planConstraintsService.coerceVisibility(groupId, repoInput.visibility),
      ownerId = currentCoroutineContext().userId(),
      pushNotificationsEnabled = repoInput.pushNotificationsEnabled,
      retentionMaxCapacity =
        planConstraintsService.coerceRetentionMaxCapacity(repoInput.retention?.maxCapacity, groupId),
      retentionMaxAgeDays = planConstraintsService.coerceRetentionMaxAgeDays(
        repoInput.retention?.maxAgeDays,
        groupId = groupId
      ),
      groupId = groupId,
      product = repoInput.product,
      sourcesSyncCron = repoInput.refreshCron?.let {
        planConstraintsService.auditCronExpression(it)
      } ?: ""
    )


    planConstraintsService.auditSourcesMaxCountPerRepository(repoInput.sources.size, groupId)

    repo = repoInput.plugins?.let {
      if (it.size > 5) {
        throw BadRequestException("Too many plugins ${it.size}, limit 5")
      }
      repo.copy(plugins = it)
    } ?: repo

    val saved = repositoryRepository.save(repo)

    sourceUseCase.createSources(
      repoInput.sources.map { it.copy(repositoryId = repo.id) },
      repo.id
    )

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
//

  suspend fun findAllByVisibilityAndLastPullSyncBefore(
    visibility: EntityVisibility,
    now: LocalDateTime,
    pageable: PageRequest
  ): List<Repository> {
    log.debug("findAllByVisibilityAndLastPullSyncBefore visibility=$visibility")
    return repositoryRepository.findAllByVisibilityAndLastPullSyncBefore(visibility, now, pageable.toPageableRequest())
  }

  override suspend fun expectsCapabilities(capabilityId: CapabilityId): Boolean {
    log.debug("expectsCapabilities capabilityId=$capabilityId")
    TODO("Not yet implemented")
  }

  override suspend fun provideAll(
    capability: UnresolvedCapability,
    pageable: PageableRequest,
    where: RepositoriesFilter?
  ): List<Repository> {
    log.debug("provideAll")
    TODO("Not yet implemented")
  }
}

fun Document.toJsonItem(
  appConfig: AppConfig,
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
      url = it.remoteDataUrl ?: createAttachmentUrl(appConfig, it.id),
      type = it.mimeType,
      length = it.size,
      duration = it.duration
    )
  }
  if (visibility === EntityVisibility.isPublic) {
    article.url = createDocumentUrl(appConfig, id)
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
