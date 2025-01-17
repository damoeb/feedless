package org.migor.feedless.source

import com.linecorp.kotlinjdsl.dsl.jpql.jpql
import com.linecorp.kotlinjdsl.querymodel.jpql.path.Path
import com.linecorp.kotlinjdsl.querymodel.jpql.predicate.Predicatable
import com.linecorp.kotlinjdsl.querymodel.jpql.sort.Sortable
import com.linecorp.kotlinjdsl.render.jpql.JpqlRenderContext
import com.linecorp.kotlinjdsl.support.spring.data.jpa.extension.createQuery
import jakarta.persistence.EntityManager
import jakarta.validation.Validation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.ResumableHarvestException
import org.migor.feedless.actions.ClickPositionActionEntity
import org.migor.feedless.actions.ClickXpathActionEntity
import org.migor.feedless.actions.DomActionEntity
import org.migor.feedless.actions.ExecuteActionEntity
import org.migor.feedless.actions.ExtractBoundingBoxActionEntity
import org.migor.feedless.actions.ExtractXpathActionEntity
import org.migor.feedless.actions.FetchActionEntity
import org.migor.feedless.actions.HeaderActionEntity
import org.migor.feedless.actions.ScrapeActionDAO
import org.migor.feedless.actions.ScrapeActionEntity
import org.migor.feedless.actions.WaitActionEntity
import org.migor.feedless.api.fromDto
import org.migor.feedless.document.DocumentDAO
import org.migor.feedless.generated.types.SortOrder
import org.migor.feedless.generated.types.SourceInput
import org.migor.feedless.generated.types.SourceOrderByInput
import org.migor.feedless.generated.types.SourceUpdateInput
import org.migor.feedless.generated.types.SourcesWhereInput
import org.migor.feedless.pipeline.PipelineJobStatus
import org.migor.feedless.pipeline.SourcePipelineJobDAO
import org.migor.feedless.pipeline.SourcePipelineJobEntity
import org.migor.feedless.plan.PlanConstraintsService
import org.migor.feedless.repository.RepositoryEntity
import org.migor.feedless.repository.RepositoryHarvester
import org.migor.feedless.scrape.LogCollector
import org.migor.feedless.user.corrId
import org.migor.feedless.util.JtsUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.BeanUtils
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*
import kotlin.coroutines.coroutineContext

@Service
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.source} & ${AppLayer.service}")
class SourceService(
  private val sourcePipelineJobDAO: SourcePipelineJobDAO,
  private val sourceDAO: SourceDAO,
  @Lazy private val repositoryHarvester: RepositoryHarvester,
  private val documentDAO: DocumentDAO,
  private val entityManager: EntityManager,
  private val planConstraintsService: PlanConstraintsService,
  private val scrapeActionDAO: ScrapeActionDAO
) {

  private val log = LoggerFactory.getLogger(SourceService::class.simpleName)

  @Transactional
  suspend fun processSourcePipeline(sourceId: UUID, jobs: List<SourcePipelineJobEntity>) {
    val corrId = coroutineContext.corrId()
    log.info("[$corrId] ${jobs.size} processSourcePipeline for source $sourceId")

    val job = jobs.first()
    job.status = PipelineJobStatus.IN_PROGRESS

    val source = withContext(Dispatchers.IO) {
      sourcePipelineJobDAO.save(job)
      sourceDAO.findByIdWithActions(sourceId)!!
    }

    try {
      try {
        repositoryHarvester.scrapeSource(patchRequestUrl(source, job.url), LogCollector())
        job.status = PipelineJobStatus.SUCCEEDED
        job.updateStatus()
        log.info("[$corrId] job ${job.id} done")
      } catch (e: ResumableHarvestException) {
        log.info("[$corrId] delaying: ${e.message}")
        job.coolDownUntil = LocalDateTime.now().plus(e.nextRetryAfter)
      }

    } catch (e: Exception) {
      log.warn("[$corrId] aborting scrape job, cause ${e.message}")
      job.status = PipelineJobStatus.FAILED
      job.updateStatus()
      job.logs = e.message
    }
    try {
      withContext(Dispatchers.IO) {
        sourcePipelineJobDAO.save(job)
      }
    } catch (e: Exception) {
      log.warn("[$corrId] ${e.message}]", e)
    }
  }

  private fun patchRequestUrl(source: SourceEntity, url: String): SourceEntity {
    val newSource = source.clone()
    newSource.actions = source.actions.mapNotNull {
      when (it) {
        is FetchActionEntity -> it.clone()
        is ExecuteActionEntity -> it.clone()
        is ClickPositionActionEntity -> it.clone()
        is ClickXpathActionEntity -> it.clone()
        is DomActionEntity -> it.clone()
        is ExtractBoundingBoxActionEntity -> it.clone()
        is ExtractXpathActionEntity -> it.clone()
        is HeaderActionEntity -> it.clone()
        is WaitActionEntity -> it.clone()
        else -> null
      }
    }.toMutableList()

    val fetchAction = newSource.actions.filterIsInstance<FetchActionEntity>().first()
    fetchAction.url = url

    return newSource
  }

  @Transactional(readOnly = true)
  suspend fun countProblematicSourcesByRepositoryId(repositoryId: UUID): Int {
    return withContext(Dispatchers.IO) {
//      sourceDAO.countByRepositoryIdAndDisabledTrue(repositoryId)
      sourceDAO.countByRepositoryIdAndLastRecordsRetrieved(repositoryId, 0)
    }
  }

  @Transactional(readOnly = true)
  suspend fun countDocumentsBySourceId(sourceId: UUID): Int {
    return withContext(Dispatchers.IO) {
      documentDAO.countBySourceId(sourceId)
    }
  }

  @Transactional
  suspend fun setErrorState(sourceId: UUID, erroneous: Boolean, message: String?) {
    withContext(Dispatchers.IO) {
      sourceDAO.setErrorState(sourceId, erroneous, message)
    }
  }

  @Transactional
  suspend fun save(source: SourceEntity): SourceEntity {
    return withContext(Dispatchers.IO) {
      sourceDAO.save(source)
    }
  }

  @Transactional(readOnly = true)
  suspend fun countAllByRepositoryId(id: UUID): Long {
    return withContext(Dispatchers.IO) {
      sourceDAO.countByRepositoryId(id)
    }
  }

  @Transactional(readOnly = true)
  suspend fun findAllByRepositoryIdFiltered(
    repositoryId: UUID,
    pageable: Pageable,
    where: SourcesWhereInput? = null,
    orders: List<SourceOrderByInput>? = null
  ): List<SourceEntity> {
    val whereStatements = mutableListOf<Predicatable>()
    val sortableStatements = mutableListOf<Sortable>()
    val query = jpql {
      where?.let {
        it.like?.let { like ->
          if (like.length > 2) {
            whereStatements.add(or(
              path(SourceEntity::title).like("%$like%"),
              path(FetchActionEntity::url).like("%$like%"),
            )
            )
          }
        }
        it.id?.let {
          it.eq?.let {
            whereStatements.add(path(SourceEntity::id).eq(UUID.fromString(it)))
          }
          it.`in`?.let {
            whereStatements.add(path(SourceEntity::id).`in`(it.map { UUID.fromString(it) }))
          }
        }

        it.latLng?.let {
          // https://postgis.net/docs/ST_Distance.html
          whereStatements.add(path(SourceEntity::latLon).isNotNull())
          it.near?.let {
            whereStatements.add(
              function(
                Double::class,
                "fl_latlon_distance",
                path(SourceEntity::latLon),
                doubleLiteral(it.point.lat),
                doubleLiteral(it.point.lng)
              )
                .lt(doubleLiteral(it.distanceKm.coerceAtMost(20.0)))
            )
          }
        }
      }
      val applySortDirection = { path: Path<*>, direction: SortOrder -> when (direction) {
        SortOrder.asc -> path.asc().nullsFirst()
        SortOrder.desc -> path.desc().nullsLast()
      } }

      orders?.let {
        sortableStatements.addAll(
          orders.map {
            if (it.title != null) {
              applySortDirection(path(SourceEntity::title), it.title)
            } else {
              if (it.lastRecordsRetrieved != null) {
                applySortDirection(path(SourceEntity::lastRecordsRetrieved), it.lastRecordsRetrieved)
              } else {
                if (it.lastRefreshedAt != null) {
                  applySortDirection(path(SourceEntity::lastRefreshedAt), it.lastRefreshedAt)
                } else {
                  throw IllegalArgumentException("Underspecified source order params")
                }
              }
            }
          }
        )
      }

      select(path(SourceEntity::id))
        .from(
          entity(SourceEntity::class),
          join(FetchActionEntity::class).on(path(FetchActionEntity::sourceId).eq(path(SourceEntity::id))))
        .whereAnd(
          path(SourceEntity::repositoryId).eq(repositoryId),
          *whereStatements.toTypedArray(),
        )
        .orderBy(
          *sortableStatements.toTypedArray(),
          path(SourceEntity::createdAt).desc()
        )
    }

    val context = JpqlRenderContext()

    return withContext(Dispatchers.IO) {
      val q = entityManager.createQuery(query, context)
      q.setMaxResults(pageable.pageSize)
      q.setFirstResult(pageable.pageSize * pageable.pageNumber)
      sourceDAO.findAllWithActionsByIdIn(q.resultList).sortedBy { it.lastRecordsRetrieved }
    }
  }

  @Transactional
  suspend fun createSources(ownerId: UUID, sourceInputs: List<SourceInput>, repository: RepositoryEntity) {
    log.info("[${coroutineContext.corrId()}] creating ${sourceInputs.size} sources")

    withContext(Dispatchers.IO) {
      val createSources = mutableListOf<SourceEntity>()
      val createScrapeActions = mutableListOf<ScrapeActionEntity>()
      sourceInputs.map { it.fromDto() }
        .map { source: SourceEntity ->
          planConstraintsService.auditScrapeRequestMaxActions(source.actions.size, ownerId)
//        planConstraintsService.auditScrapeRequestTimeout(scrapeRequest.page.timeout, ownerId)

          if (source.actions.isEmpty()) {
            throw IllegalArgumentException("flow must not be empty")
          }
          val validator = Validation.buildDefaultValidatorFactory().validator
          val invalidActions = source.actions.filter { validator.validate(it).isNotEmpty() }
          if (invalidActions.isNotEmpty()) {
            throw IllegalArgumentException("invalid actions $invalidActions")
          }

          source.repositoryId = repository.id

          if (validator.validate(source).isNotEmpty()) {
            throw IllegalArgumentException("invalid source")
          }

          val actions = source.actions.mapIndexed { index, scrapeAction ->
            scrapeAction.sourceId = source.id
            scrapeAction.pos = index
            scrapeAction
          }

          createScrapeActions.addAll(actions)
          source.actions = mutableListOf()
          createSources.add(source)
        }

      sourceDAO.saveAll(createSources)
      scrapeActionDAO.saveAll(createScrapeActions)
    }

  }

  @Transactional
  suspend fun updateSources(repository: RepositoryEntity, updateInputs: List<SourceUpdateInput>) {
    log.info("[${coroutineContext.corrId()}] updating ${updateInputs.size} sources")

    // todo check owner

    withContext(Dispatchers.IO) {
      val modifiedSources = mutableListOf<SourceEntity>()
      val deleteScrapeActions = mutableListOf<ScrapeActionEntity>()
      val saveScrapeActions = mutableListOf<ScrapeActionEntity>()

      updateInputs.map { sourceUpdate ->
        val source = sourceDAO.findById(UUID.fromString(sourceUpdate.where.id)).orElseThrow()
        if (source.repositoryId != repository.id) {
          throw IllegalArgumentException("source does not belong to repository")
        }

        var changed = false

        sourceUpdate.data.tags?.let {
          source.tags = sourceUpdate.data.tags.set.toTypedArray()
          changed = true
        }
        sourceUpdate.data.title?.let {
          source.title = sourceUpdate.data.title.set
          changed = true
        }
        sourceUpdate.data.latLng?.let { point ->
          point.set?.let {
            source.latLon = JtsUtil.createPoint(it.lat, it.lng)
          } ?: run { source.latLon = null }
          changed = true
        }
        sourceUpdate.data.disabled?.let { disabled ->
          source.disabled = disabled.set
          source.errorsInSuccession = 0
          changed = true
        }

        sourceUpdate.data.flow?.let { flow ->
          // remove old actions
          deleteScrapeActions.addAll(scrapeActionDAO.findAllBySourceId(source.id))

          flow.set?.let {
            // append new actions
            val actions = flow.set.fromDto().mapIndexed { index, scrapeAction ->
              scrapeAction.sourceId = source.id
              scrapeAction.pos = index
              scrapeAction
            }
            saveScrapeActions.addAll(actions)
          }
        }

        source.actions = mutableListOf()

        if (changed) {
          modifiedSources.add(source)
        }
      }

      scrapeActionDAO.deleteAll(deleteScrapeActions)
      scrapeActionDAO.saveAll(saveScrapeActions)
      sourceDAO.saveAll(modifiedSources)
    }
  }

  @Transactional
  suspend fun deleteAllById(repositoryId: UUID, sourceIds: List<UUID>) {
    log.info("[${coroutineContext.corrId()}] removing ${sourceIds.size} sources")
    withContext(Dispatchers.IO) {
      val sources = sourceDAO.findAllByRepositoryIdAndIdIn(repositoryId, sourceIds)
      sourceDAO.deleteAllById(sources.map { it.id })
    }
  }

  @Transactional(readOnly = true)
  suspend fun findById(sourceId: UUID): Optional<SourceEntity> {
    return withContext(Dispatchers.IO) {
      sourceDAO.findById(sourceId)
    }
  }

}


private fun SourceEntity.clone(): SourceEntity {
  val s = SourceEntity()
  BeanUtils.copyProperties(this, s, "actions", "repository")
  return s
}

private fun WaitActionEntity.clone(): WaitActionEntity {
  val e = WaitActionEntity()
  BeanUtils.copyProperties(this, e, "source")
  return e
}

private fun HeaderActionEntity.clone(): HeaderActionEntity {
  val e = HeaderActionEntity()
  BeanUtils.copyProperties(this, e, "source")
  return e
}

private fun ExtractXpathActionEntity.clone(): ExtractXpathActionEntity {
  val e = ExtractXpathActionEntity()
  BeanUtils.copyProperties(this, e, "source")
  return e
}

private fun ExtractBoundingBoxActionEntity.clone(): ExtractBoundingBoxActionEntity {
  val e = ExtractBoundingBoxActionEntity()
  BeanUtils.copyProperties(this, e, "source")
  return e
}

private fun DomActionEntity.clone(): DomActionEntity {
  val e = DomActionEntity()
  BeanUtils.copyProperties(this, e, "source")
  return e
}

private fun ClickXpathActionEntity.clone(): ClickXpathActionEntity {
  val e = ClickXpathActionEntity()
  BeanUtils.copyProperties(this, e, "source")
  return e
}

private fun ClickPositionActionEntity.clone(): ClickPositionActionEntity {
  val e = ClickPositionActionEntity()
  BeanUtils.copyProperties(this, e, "source")
  return e
}

private fun ExecuteActionEntity.clone(): ExecuteActionEntity {
  val e = ExecuteActionEntity()
  BeanUtils.copyProperties(this, e, "source")
  return e
}

private fun FetchActionEntity.clone(): FetchActionEntity {
  val e = FetchActionEntity()
  BeanUtils.copyProperties(this, e, "source")
  return e
}

