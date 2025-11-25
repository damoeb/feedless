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
import org.migor.feedless.actions.FetchAction
import org.migor.feedless.api.mapper.fromDto
import org.migor.feedless.api.mapper.toSource
import org.migor.feedless.data.jpa.JtsUtil
import org.migor.feedless.data.jpa.document.DocumentDAO
import org.migor.feedless.data.jpa.pipelineJob.SourcePipelineJobDAO
import org.migor.feedless.data.jpa.pipelineJob.toEntity
import org.migor.feedless.data.jpa.source.SourceDAO
import org.migor.feedless.data.jpa.source.SourceEntity
import org.migor.feedless.data.jpa.source.actions.FetchActionEntity
import org.migor.feedless.data.jpa.source.actions.ScrapeActionDAO
import org.migor.feedless.data.jpa.source.actions.ScrapeActionEntity
import org.migor.feedless.data.jpa.source.toDomain
import org.migor.feedless.data.jpa.source.toEntity
import org.migor.feedless.generated.types.SortOrder
import org.migor.feedless.generated.types.SourceInput
import org.migor.feedless.generated.types.SourceOrderByInput
import org.migor.feedless.generated.types.SourceUpdateInput
import org.migor.feedless.generated.types.SourcesWhereInput
import org.migor.feedless.pipelineJob.PipelineJobStatus
import org.migor.feedless.pipelineJob.SourcePipelineJob
import org.migor.feedless.plan.PlanConstraintsService
import org.migor.feedless.repository.RepositoryHarvester
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.scrape.LogCollector
import org.migor.feedless.user.UserId
import org.migor.feedless.user.corrId
import org.slf4j.LoggerFactory
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

  fun trackedRepositories() {
//    sourceDAO.getRepositoryHealthByRepositoryIdIn()
  }

  @Transactional
  suspend fun processSourcePipeline(sourceId: SourceId, jobs: List<SourcePipelineJob>) {
    val corrId = coroutineContext.corrId()
    log.info("[$corrId] ${jobs.size} processSourcePipeline for source $sourceId")

    val job = jobs.first().copy(
      status = PipelineJobStatus.IN_PROGRESS
    )

    val source = withContext(Dispatchers.IO) {
      sourcePipelineJobDAO.save(job.toEntity())
      sourceDAO.findByIdWithActions(sourceId.uuid)!!.toDomain()
    }

    val updatedJob = try {
      try {
        repositoryHarvester.scrapeSource(patchRequestUrl(source, job.url), LogCollector())
        log.info("[$corrId] job ${job.id} done")
        job.copy(
          status = PipelineJobStatus.SUCCEEDED
        )
      } catch (e: ResumableHarvestException) {
        log.info("[$corrId] delaying: ${e.message}")
        job.copy(coolDownUntil = LocalDateTime.now().plus(e.nextRetryAfter))
      }
    } catch (e: Exception) {
      log.warn("[$corrId] aborting scrape job, cause ${e.message}")
      job.copy(
        status = PipelineJobStatus.FAILED,
        logs = e.message
      )
    }
    try {
      withContext(Dispatchers.IO) {
        sourcePipelineJobDAO.save(updatedJob.toEntity())
      }
    } catch (e: Exception) {
      log.warn("[$corrId] ${e.message}]", e)
    }
  }

  private fun patchRequestUrl(source: Source, url: String): Source {
    val newSource = source.copy(
      actions = source.actions.map {
        when (it) {
          is FetchAction -> it.copy(
            url = url,
          )

          else -> it
        }
      }
    )
    return newSource
  }

  @Transactional(readOnly = true)
  suspend fun countProblematicSourcesByRepositoryId(repositoryId: RepositoryId): Int {
    return withContext(Dispatchers.IO) {
//      sourceDAO.countByRepositoryIdAndDisabledTrue(repositoryId)
      sourceDAO.countByRepositoryIdAndLastRecordsRetrieved(repositoryId.uuid, 0)
    }
  }

  @Transactional(readOnly = true)
  suspend fun countDocumentsBySourceId(sourceId: SourceId): Int {
    return withContext(Dispatchers.IO) {
      documentDAO.countBySourceId(sourceId.uuid)
    }
  }

  @Transactional
  suspend fun setErrorState(sourceId: SourceId, erroneous: Boolean, message: String?) {
    withContext(Dispatchers.IO) {
      sourceDAO.setErrorState(sourceId.uuid, erroneous, message)
    }
  }

  @Transactional
  suspend fun save(source: Source): Source {
    return withContext(Dispatchers.IO) {
      sourceDAO.save(source.toEntity()).toDomain()
    }
  }

  @Transactional(readOnly = true)
  suspend fun countAllByRepositoryId(id: RepositoryId): Long {
    return withContext(Dispatchers.IO) {
      sourceDAO.countByRepositoryId(id.uuid)
    }
  }

  @Transactional(readOnly = true)
  suspend fun findAllByRepositoryIdFiltered(
    repositoryId: RepositoryId,
    pageable: Pageable,
    where: SourcesWhereInput? = null,
    orders: List<SourceOrderByInput>? = null
  ): List<Source> {
    val whereStatements = mutableListOf<Predicatable>()
    val sortableStatements = mutableListOf<Sortable>()
    val query = jpql {
      where?.let {
        it.like?.let { like ->
          if (like.length > 2) {
            whereStatements.add(
              or(
                path(SourceEntity::title).like("%$like%"),
                path(FetchActionEntity::url).like("%$like%"),
              )
            )
          }
        }
        it.disabled?.let {
          whereStatements.add(
            path(SourceEntity::disabled).eq(it),
          )
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
      val applySortDirection = { path: Path<*>, direction: SortOrder ->
        when (direction) {
          SortOrder.asc -> path.asc().nullsFirst()
          SortOrder.desc -> path.desc().nullsLast()
        }
      }

      orders?.let {
        sortableStatements.addAll(
          orders.map {
            if (it.title != null) {
              applySortDirection(path(SourceEntity::title), it.title!!)
            } else {
              if (it.lastRecordsRetrieved != null) {
                applySortDirection(path(SourceEntity::lastRecordsRetrieved), it.lastRecordsRetrieved!!)
              } else {
                if (it.lastRefreshedAt != null) {
                  applySortDirection(path(SourceEntity::lastRefreshedAt), it.lastRefreshedAt!!)
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
          join(FetchActionEntity::class).on(path(FetchActionEntity::sourceId).eq(path(SourceEntity::id)))
        )
        .whereAnd(
          path(SourceEntity::repositoryId).eq(repositoryId.uuid),
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
        .map { it.toDomain() }
    }
  }

  @Transactional
  suspend fun createSources(ownerId: UserId, sourceInputs: List<SourceInput>, repositoryId: RepositoryId) {
    log.info("[${coroutineContext.corrId()}] creating ${sourceInputs.size} sources")

    withContext(Dispatchers.IO) {
      val createSources = mutableListOf<SourceEntity>()
      val createScrapeActions = mutableListOf<ScrapeActionEntity>()
      sourceInputs.map { it.toSource() }
        .map { source: Source ->
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

          // Convert to entity
          val sourceEntity = source.toEntity()
          sourceEntity.repositoryId = repositoryId.uuid

          if (validator.validate(sourceEntity).isNotEmpty()) {
            throw IllegalArgumentException("invalid source")
          }

          val actions = source.actions.mapIndexed { index, scrapeAction ->
            val actionEntity = scrapeAction.toEntity()
            actionEntity.sourceId = sourceEntity.id
            actionEntity.pos = index
            actionEntity
          }

          createScrapeActions.addAll(actions)
          sourceEntity.actions = mutableListOf()
          createSources.add(sourceEntity)
        }

      sourceDAO.saveAll(createSources)
      scrapeActionDAO.saveAll(createScrapeActions)
    }

  }

  @Transactional
  suspend fun updateSources(repositoryId: RepositoryId, updateInputs: List<SourceUpdateInput>) {
    log.info("[${coroutineContext.corrId()}] updating ${updateInputs.size} sources")

    // todo check owner

    withContext(Dispatchers.IO) {
      val modifiedSources = mutableListOf<SourceEntity>()
      val deleteScrapeActions = mutableListOf<ScrapeActionEntity>()
      val saveScrapeActions = mutableListOf<ScrapeActionEntity>()

      updateInputs.map { sourceUpdate ->
        val source = sourceDAO.findById(UUID.fromString(sourceUpdate.where.id)).orElseThrow()
        if (source.repositoryId != repositoryId.uuid) {
          throw IllegalArgumentException("source does not belong to repository")
        }

        var changed = false

        sourceUpdate.data.tags?.let {
          source.tags = it.set.toTypedArray()
          changed = true
        }
        sourceUpdate.data.title?.let {
          source.title = it.set
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
            val actions = flow.set?.fromDto()?.mapIndexed { index, scrapeAction ->
              val actionEntity = scrapeAction.toEntity()
              actionEntity.sourceId = source.id
              actionEntity.pos = index
              actionEntity
            }
            actions?.let {
              saveScrapeActions.addAll(actions)
            }
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
  suspend fun deleteAllById(repositoryId: RepositoryId, sourceIds: List<SourceId>) {
    log.info("[${coroutineContext.corrId()}] removing ${sourceIds.size} sources")
    withContext(Dispatchers.IO) {
      val sources = sourceDAO.findAllByRepositoryIdAndIdIn(repositoryId.uuid, sourceIds.map { it.uuid })
      sourceDAO.deleteAllById(sources.map { it.id })
    }
  }

  @Transactional(readOnly = true)
  suspend fun findById(sourceId: SourceId): Source? {
    return withContext(Dispatchers.IO) {
      sourceDAO.findByIdWithActions(sourceId.uuid)?.toDomain()
    }
  }

}
