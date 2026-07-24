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
import org.migor.feedless.util.toOffsetDateTime
import org.springframework.stereotype.Component
import org.migor.feedless.http.api.model.Vertical as HttpVertical
import org.migor.feedless.http.api.model.VerticalFilter as HttpVerticalFilter
import org.migor.feedless.http.api.model.Visibility as HttpVisibility

@Component
class HttpRepositoryMapper(
  private val scrapeFlowMapper: HttpScrapeFlowMapper,
) {

  fun toHttp(repo: Repository, currentUserIsOwner: Boolean): HttpRepository =
    HttpRepository(
      id = repo.id.uuid,
      title = repo.title,
      description = repo.description,
      // shareKey is a capability secret: omit it entirely for non-owners rather than
      // shipping an empty string that reads like a real (blank) key.
      shareKey = repo.shareKey.takeIf { currentUserIsOwner },
      ownerId = repo.ownerId.uuid,
      product = toHttpVertical(repo.product),
      visibility = toHttpVisibility(repo.visibility),
      refreshCron = repo.sourcesSyncCron,
      tags = repo.tags.toList(),
      createdAt = repo.createdAt.toOffsetDateTime(),
      lastUpdatedAt = repo.lastUpdatedAt.toOffsetDateTime(),
      nextUpdateAt = repo.triggerScheduledNextAt?.toOffsetDateTime(),
      documentCount = repo.documentCountSinceCreation.toLong(),
      archived = repo.archived,
      pullsPerMonth = repo.pullsPerMonth,
      currentUserIsOwner = currentUserIsOwner,
      pushNotificationsEnabled = repo.pushNotificationsEnabled,
      // Not computed on this path — omit rather than report a hardcoded 0.
      sourcesCount = null,
      sourcesCountWithProblems = null,
      disabledFrom = repo.disabledFrom?.toOffsetDateTime(),
    )

  fun toDomainCreate(body: RepositoryCreate): DomainRepositoryCreate =
    DomainRepositoryCreate(
      product = Vertical.valueOf(body.product.value),
      title = body.title,
      description = body.description,
      sources = body.sources.map { scrapeFlowMapper.toDomainSource(it) },
      refreshCron = body.refreshCron,
      visibility = body.visibility?.let { toDomainVisibility(it) },
      pushNotificationsEnabled = body.pushNotificationsMuted == true,
      retention = body.retention?.let {
        RepositoryRetention(maxCapacity = it.maxCapacity, maxAgeDays = it.maxAgeDays)
      },
    )

  fun toDomainUpdate(update: RepositoryUpdate): DomainRepositoryUpdate =
    DomainRepositoryUpdate(
      title = update.title,
      description = update.description,
      refreshCron = update.refreshCron,
      pushNotificationsEnabled = update.pushNotificationsMuted,
      visibility = update.visibility?.let { toDomainVisibility(it) },
      retentionMaxCapacity = update.retention?.maxCapacity,
      retentionMaxAgeDays = update.retention?.maxAgeDays,
    )

  /** `all` is a filter value; a stored repository always has a concrete product. */
  fun toDomainVertical(filter: HttpVerticalFilter): Vertical = Vertical.valueOf(filter.value)

  private fun toHttpVertical(product: Vertical): HttpVertical =
    HttpVertical.entries.firstOrNull { it.value == product.name }
      ?: throw IllegalStateException("repository has non-product vertical '$product'")

  fun toDomainVisibility(visibility: HttpVisibility): EntityVisibility = when (visibility) {
    HttpVisibility.private -> EntityVisibility.isPrivate
    HttpVisibility.public -> EntityVisibility.isPublic
  }

  private fun toHttpVisibility(visibility: EntityVisibility): HttpVisibility = when (visibility) {
    EntityVisibility.isPrivate -> HttpVisibility.private
    EntityVisibility.isPublic -> HttpVisibility.public
  }
}
