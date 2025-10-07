package org.migor.feedless.repository

import com.linecorp.kotlinjdsl.querymodel.jpql.predicate.Predicatable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.lang3.BooleanUtils
import org.apache.commons.lang3.StringUtils
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.BadRequestException
import org.migor.feedless.NotFoundException
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.Vertical
import org.migor.feedless.actions.PluginExecutionJsonEntity
import org.migor.feedless.attachment.createAttachmentUrl
import org.migor.feedless.common.PropertyService
import org.migor.feedless.config.CacheNames
import org.migor.feedless.data.jpa.enums.EntityVisibility
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
import org.migor.feedless.generated.types.PluginExecutionParamsInput
import org.migor.feedless.generated.types.RecordOrderByInput
import org.migor.feedless.generated.types.RecordsWhereInput
import org.migor.feedless.generated.types.RepositoriesWhereInput
import org.migor.feedless.generated.types.Repository
import org.migor.feedless.generated.types.RepositoryCreateInput
import org.migor.feedless.generated.types.RepositoryUpdateDataInput
import org.migor.feedless.generated.types.Visibility
import org.migor.feedless.plan.PlanConstraintsService
import org.migor.feedless.session.SessionService
import org.migor.feedless.source.SourceId
import org.migor.feedless.source.SourceService
import org.migor.feedless.user.UserEntity
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


data class RepositoryId(val value: UUID) {
  constructor(value: String) : this(UUID.fromString(value))
}

@Service
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.repository} & ${AppLayer.service}")
class RepositoryService(
//  private var userDAO: UserDAO,
  private var repositoryDAO: RepositoryDAO,
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

    val ownerId = UserId(getActualUserOrDefaultUser().id)
    val totalCount = withContext(Dispatchers.IO) {
      repositoryDAO.countByOwnerId(ownerId.value)
    }
    planConstraintsService.auditRepositoryMaxCount(totalCount, ownerId)
    if (planConstraintsService.violatesRepositoriesMaxActiveCount(ownerId)) {
      log.info("[${coroutineContext.corrId()}] violates maxActiveCount")
      throw IllegalArgumentException("Too many active repositories")
//      log.info("[$corrId] violates maxActiveCount, archiving oldest")
//      RepositoryDAO.updateArchivedForOldestActive(ownerId)
    }
    return data.map { createRepository(ownerId, it).toDto(true) }
  }

  @Cacheable(value = [CacheNames.FEED_SHORT_TTL], key = "\"repo/\" + #repositoryId + #tags")
  suspend fun getFeedByRepositoryId(
    repositoryId: RepositoryId,
    page: Int,
    tags: List<String>,
    where: RecordsWhereInput?,
    orderBy: RecordOrderByInput?,
  ): JsonFeed {
    val repository = context.getBean(RepositoryService::class.java).findById(repositoryId).orElseThrow()

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
  ): List<RepositoryEntity> {
    log.debug("userId=$userId")

    return withContext(Dispatchers.IO) {
      repositoryDAO.findPage(pageable) {
        val whereStatements = mutableListOf<Predicatable>()
        where?.let {
          where.visibility?.let { visibility ->
            visibility.`in`?.let {
              whereStatements.add(
                path(RepositoryEntity::visibility).`in`(visibility.`in`!!.map { it.fromDto() }),
              )
            }
          }

          userId?.let {
            whereStatements.add(
              path(RepositoryEntity::ownerId).eq(userId.value)
            )
          }

//        where.text?.let { text ->
//          whereStatements.add(
//            or(
//              function(
//                Boolean::class,
//                "fl_fulltext_search",
//                path(RepositoryEntity::title),
//              )
//                .eq(true),
//              function(
//                Boolean::class,
//                "fl_fulltext_search",
//                path(RepositoryEntity::description),
//              )
//                .eq(true)
//            )
//          )
//        }

          where.product?.let {
            it.eq?.let {
              whereStatements.add(
                path(RepositoryEntity::product).eq(it.fromDto())
              )
            }
            it.`in`?.let { products ->
              whereStatements.add(
                path(RepositoryEntity::product).`in`(products.map { it.fromDto() })
              )
            }
          }
          where.tags?.let {
            it.every?.let { every ->
              whereStatements.add(
                function(
                  Boolean::class,
                  "fl_array_contains",
                  path(RepositoryEntity::tags),
                  every,
                  true
                )
                  .eq(true)
              )
            }
            it.some?.let { some ->
              whereStatements.add(
                function(
                  Boolean::class,
                  "fl_array_contains",
                  path(RepositoryEntity::tags),
                  some,
                  false
                )
                  .eq(true)
              )
            }
          }
        }

        select(
          entity(RepositoryEntity::class)
        ).from(
          entity(RepositoryEntity::class)
        ).whereAnd(
          *whereStatements.toTypedArray(),
          or(
            path(RepositoryEntity::visibility).eq(EntityVisibility.isPublic),
            path(RepositoryEntity::ownerId).eq(userId?.value),
          )
        ).orderBy(
          path(RepositoryEntity::lastUpdatedAt).desc()
        )
      }
    }.toList().filterNotNull()
  }

  @Transactional(readOnly = true)
  suspend fun findById(repositoryId: RepositoryId): Optional<RepositoryEntity> {
    return withContext(Dispatchers.IO) {
      repositoryDAO.findById(repositoryId.value)
    }
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
    withContext(Dispatchers.IO) {
      val repository = repositoryDAO.findById(repositoryId.value).orElseThrow()
      if (repository.ownerId != coroutineContext.userId().value) {
        throw PermissionDeniedException("not authorized")
      }
      log.info("[${coroutineContext.corrId()}] removing repository $repositoryId")
      repositoryDAO.delete(repository)
    }
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
    val repository = context.getBean(RepositoryService::class.java).findById(id)
      .orElseThrow { NotFoundException("Repository $id not found") }
    val corrId = coroutineContext.corrId()
    if (repository.ownerId != coroutineContext.userId().value) {
      throw PermissionDeniedException("not authorized")
    }
    log.info("[$corrId] update $id")
    repository.lastUpdatedAt = LocalDateTime.now()
    data.title?.set?.let { repository.title = it }
    data.description?.set?.let { repository.description = it }
    val product = sessionService.activeProductFromRequest()!!
    data.refreshCron?.let {
      it.set?.let {
        repository.sourcesSyncCron = planConstraintsService.auditCronExpression(it)
        repository.triggerScheduledNextAt = calculateScheduledNextAt(
          it, UserId(repository.ownerId),
          product,
          repository.lastUpdatedAt
        )
      }
    }

    data.pushNotificationsMuted?.let {
      repository.pushNotificationsEnabled = it.set
    }

    data.visibility?.set?.let { repository.visibility = planConstraintsService.coerceVisibility(it.fromDto()) }
    data.plugins?.let { plugins ->
      val newPlugins = plugins.map { it.fromDto() }.sortedBy { it.id }.toMutableList()
      if (newPlugins != repository.plugins) {
        repository.plugins = newPlugins
        log.info("[${coroutineContext.corrId()}] plugins $newPlugins")
      }
    }
    data.nextUpdateAt?.let {
      val next = it.set?.toLocalDateTime() ?: LocalDateTime.now()
      repository.triggerScheduledNextAt = planConstraintsService.coerceMinScheduledNextAt(
        repository.lastUpdatedAt,
        next,
        UserId(repository.ownerId),
        product
      )
      log.info("[${coroutineContext.corrId()}] nextUpdateAt ${repository.triggerScheduledNextAt}")
    }
    data.retention?.let {
      it.maxAgeDays?.let {
        repository.retentionMaxAgeDays = it.set
        log.info("[${coroutineContext.corrId()}] retentionMaxAgeDays ${it.set}")
      }
      it.maxCapacity?.let {
        repository.retentionMaxCapacity = it.set
        log.info("[${coroutineContext.corrId()}] retentionMaxItems ${it.set}")
      }
      if (it.maxAgeDays != null || it.maxCapacity != null) {
        documentService.applyRetentionStrategy(RepositoryId(repository.id))
      }
    }

    data.sources?.let {
      it.add?.let {
        sourceService.createSources(UserId(repository.ownerId), it, repository)
      }
      it.update?.let {
        sourceService.updateSources(repository, it)
      }
      it.remove?.let {
        sourceService.deleteAllById(RepositoryId(repository.id), it.map { SourceId(it) })
      }
    }
    context.getBean(RepositoryService::class.java).save(repository)
  }

  @Transactional(readOnly = true)
  fun countAll(userId: UserId?, product: Vertical): Int {
    return userId
      ?.let { repositoryDAO.countAllByOwnerIdAndProduct(it.value, product) }
      ?: repositoryDAO.countAllByVisibility(EntityVisibility.isPublic)
  }

  @Transactional
  suspend fun updatePullsFromAnalytics(repositoryId: UUID, pulls: Int) {
    withContext(Dispatchers.IO) {
      val repository = repositoryDAO.findById(repositoryId).orElseThrow()
      repository.pullsPerMonth = pulls
      repository.lastPullSync = LocalDateTime.now()
      repositoryDAO.save(repository)
    }
  }

  private suspend fun getActualUserOrDefaultUser(): UserEntity {
    return coroutineContext.userIdOptional()?.let {
      sessionService.user()
    } ?: userService.getAnonymousUser().also { log.debug("[${coroutineContext.corrId()}] fallback to user anonymous") }
  }

  private suspend fun createRepository(
    ownerId: UserId,
    repoInput: RepositoryCreateInput
  ): RepositoryEntity {
    val repo = RepositoryEntity()

    repo.shareKey = newCorrId(10)
    repo.title = repoInput.title
    repo.description = repoInput.description
    repo.visibility = planConstraintsService.coerceVisibility(repoInput.visibility?.fromDto())
    val product = sessionService.activeProductFromRequest()!!

    planConstraintsService.auditSourcesMaxCountPerRepository(repoInput.sources.size, ownerId, product)
    repo.ownerId = ownerId.value
    repoInput.plugins?.let {
      if (it.size > 5) {
        throw BadRequestException("Too many plugins ${it.size}, limit 5")
      }
      repo.plugins = it.map { plugin -> plugin.fromDto() }
    }
    repo.shareKey = newCorrId(10)
//    if (subInput.withShareKey) {
//      newCorrId(10)
//    } else {
//      ""
//    }

    repo.pushNotificationsEnabled = BooleanUtils.isTrue(repoInput.pushNotificationsMuted)

    repo.sourcesSyncCron = repoInput.refreshCron?.let {
      planConstraintsService.auditCronExpression(repoInput.refreshCron ?: "")
    } ?: ""
    repo.retentionMaxCapacity =
      planConstraintsService.coerceRetentionMaxCapacity(repoInput.retention?.maxCapacity, ownerId, product)
    repo.retentionMaxAgeDays = planConstraintsService.coerceRetentionMaxAgeDays(
      repoInput.retention?.maxAgeDays,
      ownerId,
      product
    )
    repo.product = repoInput.product.fromDto()

    val saved = withContext(Dispatchers.IO) {
      repositoryDAO.save(repo)
    }

    sourceService.createSources(ownerId, repoInput.sources, repo)


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
  suspend fun findBySourceId(sourceId: SourceId): RepositoryEntity? {
    return withContext(Dispatchers.IO) {
      repositoryDAO.findBySourceId(sourceId.value)
    }
  }

  @Transactional(readOnly = true)
  fun findAllByVisibilityAndLastPullSyncBefore(
    visibility: EntityVisibility,
    now: LocalDateTime,
    pageable: PageRequest
  ): List<RepositoryEntity> {
    return repositoryDAO.findAllByVisibilityAndLastPullSyncBefore(visibility, now, pageable)
  }

  @Transactional(readOnly = true)
  fun findAllWhereNextHarvestIsDue(now: LocalDateTime, pageable: PageRequest): List<RepositoryEntity> {
    return repositoryDAO.findAllWhereNextHarvestIsDue(now, pageable)
  }

  @Transactional(readOnly = true)
  suspend fun findByDocumentId(documentId: UUID): RepositoryEntity? {
    return withContext(Dispatchers.IO) {
      repositoryDAO.findByDocumentId(documentId)
    }
  }

  @Transactional
  suspend fun save(repository: RepositoryEntity): RepositoryEntity {
    return withContext(Dispatchers.IO) {
      repositoryDAO.save(repository)
    }
  }

  @Transactional(readOnly = true)
  suspend fun findByTitleAndOwnerId(title: String, ownerId: UUID): RepositoryEntity? {
    return withContext(Dispatchers.IO) {
      repositoryDAO.findByTitleAndOwnerId(title, ownerId)
    }
  }
}

private fun Visibility.fromDto(): EntityVisibility {
  return when (this) {
    Visibility.isPublic -> EntityVisibility.isPublic
    Visibility.isPrivate -> EntityVisibility.isPrivate
  }
}

fun PluginExecutionInput.fromDto(): PluginExecution {
  return PluginExecution(id = pluginId, params = params.toEntity())
}

fun PluginExecutionParamsInput.toEntity(): PluginExecutionJsonEntity {
  return PluginExecutionJsonEntity(
    org_feedless_filter = org_feedless_filter,
    org_feedless_feed = org_feedless_feed,
    org_feedless_diff_records = org_feedless_diff_records,
    jsonData = jsonData,
    org_feedless_conditional_tag = org_feedless_conditional_tag,
    org_feedless_fulltext = org_feedless_fulltext
  )
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
