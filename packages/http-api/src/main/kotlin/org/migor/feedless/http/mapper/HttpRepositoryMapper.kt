package org.migor.feedless.http.mapper

import org.migor.feedless.EntityVisibility
import org.migor.feedless.Vertical
import org.migor.feedless.http.api.model.Repository as HttpRepository
import org.migor.feedless.http.api.model.RepositoryCreate
import org.migor.feedless.http.api.model.RepositoryUpdate
import org.migor.feedless.repository.Repository
import org.migor.feedless.repository.RepositoryCreate as DomainRepositoryCreate
import org.migor.feedless.repository.RepositoryRetention
import org.migor.feedless.repository.RepositoryUpdate as DomainRepositoryUpdate
import org.migor.feedless.user.UserId
import org.migor.feedless.util.toMillis
import org.springframework.stereotype.Component

@Component
class HttpRepositoryMapper(
  private val scrapeFlowMapper: HttpScrapeFlowMapper,
) {

  fun toHttp(repo: Repository, currentUserIsOwner: Boolean): HttpRepository =
    HttpRepository(
      id = repo.id.uuid,
      title = repo.title,
      description = repo.description,
      shareKey = if (currentUserIsOwner) repo.shareKey else "",
      ownerId = repo.ownerId.uuid,
      product = org.migor.feedless.http.api.model.Vertical.valueOf(repo.product.name),
      visibility = org.migor.feedless.http.api.model.Visibility.valueOf(repo.visibility.name),
      refreshCron = repo.sourcesSyncCron,
      tags = repo.tags.toList(),
      createdAt = repo.createdAt.toMillis(),
      lastUpdatedAt = repo.lastUpdatedAt.toMillis(),
      nextUpdateAt = repo.triggerScheduledNextAt?.toMillis(),
      documentCount = repo.documentCountSinceCreation.toLong(),
      archived = repo.archived,
      pullsPerMonth = repo.pullsPerMonth,
      currentUserIsOwner = currentUserIsOwner,
      pushNotificationsEnabled = repo.pushNotificationsEnabled,
      sourcesCount = 0,
      sourcesCountWithProblems = 0,
      disabledFrom = repo.disabledFrom?.toMillis(),
    )

  fun toDomainCreates(bodies: List<RepositoryCreate>): List<DomainRepositoryCreate> =
    bodies.map { body ->
      DomainRepositoryCreate(
        product = Vertical.valueOf(body.product.name),
        title = body.title,
        description = body.description,
        sources = body.sources.map { scrapeFlowMapper.toDomainSource(it) },
        refreshCron = body.refreshCron,
        visibility = body.visibility?.let { EntityVisibility.valueOf(it.name) },
        pushNotificationsEnabled = body.pushNotificationsMuted == true,
        retention = body.retention?.let {
          RepositoryRetention(maxCapacity = it.maxCapacity, maxAgeDays = it.maxAgeDays)
        },
      )
    }

  fun toDomainUpdate(update: RepositoryUpdate): DomainRepositoryUpdate =
    DomainRepositoryUpdate(
      title = update.title,
      description = update.description,
      refreshCron = update.refreshCron,
      pushNotificationsEnabled = update.pushNotificationsMuted,
      visibility = update.visibility?.let { EntityVisibility.valueOf(it.name) },
      retentionMaxCapacity = update.retention?.maxCapacity,
      retentionMaxAgeDays = update.retention?.maxAgeDays,
    )
}
