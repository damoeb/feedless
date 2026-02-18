package org.migor.feedless.repository

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import org.apache.commons.lang3.BooleanUtils
import org.apache.commons.lang3.StringUtils
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.BadRequestException
import org.migor.feedless.EntityVisibility
import org.migor.feedless.PageableRequest
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.Vertical
import org.migor.feedless.actions.PluginExecutionJson
import org.migor.feedless.api.createDocumentUrl
import org.migor.feedless.api.fromDto
import org.migor.feedless.capability.CapabilityId
import org.migor.feedless.capability.UnresolvedCapability
import org.migor.feedless.common.PropertyService
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
import org.migor.feedless.generated.types.PluginExecutionInput
import org.migor.feedless.generated.types.PluginExecutionParamsInput
import org.migor.feedless.generated.types.RepositoryCreateInput
import org.migor.feedless.generated.types.RepositoryUpdateDataInput
import org.migor.feedless.group.GroupId
import org.migor.feedless.pipeline.plugins.createAttachmentUrl
import org.migor.feedless.pipelineJob.PluginExecution
import org.migor.feedless.plan.PlanConstraintsService
import org.migor.feedless.source.SourceId
import org.migor.feedless.source.SourceUseCase
import org.migor.feedless.user.UserId
import org.migor.feedless.user.groupId
import org.migor.feedless.user.userId
import org.migor.feedless.util.CryptUtil.newCorrId
import org.migor.feedless.util.toLocalDateTime
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
  private val propertyService: PropertyService,
  private val sourceUseCase: SourceUseCase,
  private val repositoryGuard: RepositoryGuard,
) : RepositoryProvider {

  private val log = LoggerFactory.getLogger(RepositoryUseCase::class.simpleName)

  suspend fun create(data: List<RepositoryCreateInput>): List<Repository> =
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

  @Cacheable(value = [CacheNames.FEED_SHORT_TTL], key = "\"repo/\" + #repositoryId + #tags")
  suspend fun getFeedByRepositoryId(
    repositoryId: RepositoryId,
    page: Int,
    filter: DocumentsFilter?,
    order: RecordOrderBy?,
  ): JsonFeed {
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
      ).map { it.toJsonItem(propertyService, repository.visibility) }.toList()

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

  suspend fun updateRepository(id: RepositoryId, data: RepositoryUpdateDataInput) {
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
    repository = data.title?.set?.let { repository.copy(title = it) } ?: repository
    repository = data.description?.set?.let { repository.copy(description = it) } ?: repository

    val groupId = currentCoroutineContext().groupId()
    repository = data.refreshCron?.let {
      it.set?.let {
        repository.copy(
          sourcesSyncCron = planConstraintsService.auditCronExpression(it),
          triggerScheduledNextAt = calculateScheduledNextAt(
            it,
            groupId,
            repository.lastUpdatedAt
          )
        )
      }
    } ?: repository

    repository = data.pushNotificationsMuted?.let {
      repository.copy(pushNotificationsEnabled = it.set)
    } ?: repository

    repository =
      data.visibility?.set?.let {
        repository.copy(
          visibility = planConstraintsService.coerceVisibility(
            groupId,
            it.fromDto()
          )
        )
      }
        ?: repository
    repository = data.plugins?.let { plugins ->
      val newPlugins = plugins.map { it.fromDto() }.sortedBy { it.id }.toMutableList()
      if (newPlugins != repository.plugins) {
        log.info("plugins $newPlugins")
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
        groupId
      )
      log.info("nextUpdateAt $nextAt")
      repository.copy(triggerScheduledNextAt = nextAt)
    } ?: repository

    repository = data.retention?.let { retention ->
      var updated = repository
      retention.maxAgeDays?.let {
        log.info("retentionMaxAgeDays ${it.set}")
        updated = updated.copy(retentionMaxAgeDays = it.set)
      }
      retention.maxCapacity?.let {
        log.info("retentionMaxItems ${it.set}")
        updated = updated.copy(retentionMaxCapacity = it.set)
      }
      if (retention.maxAgeDays != null || retention.maxCapacity != null) {
        documentUseCase.applyRetentionStrategy(repository.id)
      }
      updated
    } ?: repository

    data.sources?.let {
      it.add?.let {
        sourceUseCase.createSources(it, repository.id)
      }
      it.update?.let {
        sourceUseCase.updateSources(repository.id, it)
      }
      it.remove?.let {
        sourceUseCase.deleteAllById(repository.id, it.map { SourceId(it) })
      }
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
    repoInput: RepositoryCreateInput
  ): Repository {

    val product = repoInput.product.fromDto()
    val groupId = currentCoroutineContext().groupId()
    var repo = Repository(
      shareKey = newCorrId(9),
      title = repoInput.title,
      description = repoInput.description,
      visibility = planConstraintsService.coerceVisibility(groupId, repoInput.visibility?.fromDto()),
      ownerId = currentCoroutineContext().userId(),
      pushNotificationsEnabled = BooleanUtils.isTrue(repoInput.pushNotificationsMuted),
      retentionMaxCapacity =
        planConstraintsService.coerceRetentionMaxCapacity(repoInput.retention?.maxCapacity, groupId),
      retentionMaxAgeDays = planConstraintsService.coerceRetentionMaxAgeDays(
        repoInput.retention?.maxAgeDays,
        groupId = groupId
      ),
      groupId = groupId,
      product = product,
      sourcesSyncCron = repoInput.refreshCron?.let {
        planConstraintsService.auditCronExpression(repoInput.refreshCron ?: "")
      } ?: ""
    )


    planConstraintsService.auditSourcesMaxCountPerRepository(repoInput.sources.size, groupId)

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

    val saved = repositoryRepository.save(repo)

    sourceUseCase.createSources(repoInput.sources, repo.id)


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

fun PluginExecutionInput.fromDto(): PluginExecution {
  return PluginExecution(id = pluginId, params = params.toParams())
}

fun PluginExecutionParamsInput.toParams(): PluginExecutionJson {
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
