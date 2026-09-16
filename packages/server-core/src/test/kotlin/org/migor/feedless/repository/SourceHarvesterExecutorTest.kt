package org.migor.feedless.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.migor.feedless.capability.MdcKeys
import org.migor.feedless.group.GroupId
import org.migor.feedless.source.Source
import org.migor.feedless.source.SourceRepository
import org.migor.feedless.user.UserId
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doSuspendableAnswer
import org.mockito.kotlin.mock
import org.slf4j.MDC
import org.springframework.scheduling.annotation.Scheduled
import java.util.Collections
import java.util.concurrent.atomic.AtomicInteger

class SourceHarvesterExecutorTest {

  private val repository = Repository(title = "r", ownerId = UserId(), groupId = GroupId())

  private fun sources(n: Int) = (1..n).map { Source(title = "s$it", repositoryId = repository.id) }

  @Test
  fun `verify refreshSubscriptions is annotated with scheduled`() {
    val method = SourceHarvesterExecutor::class.java.declaredMethods.first { it.name == "refreshSubscriptions" }
    assertThat(method.getAnnotation(Scheduled::class.java)).isNotNull()
  }

  @Test
  fun `each harvest of a run logs under a child of the run's correlation id`() {
    MDC.clear()
    val sourceRepository = mock<SourceRepository> { on { findAllDueForHarvest(any(), any()) } doReturn sources(2) }
    val repositoryRepository = mock<RepositoryRepository> { on { findById(any()) } doReturn repository }
    val seen = Collections.synchronizedList(mutableListOf<String?>())
    val harvester = mock<RepositoryHarvester> {
      onBlocking { harvestScheduled(any()) } doSuspendableAnswer {
        seen.add(withContext(Dispatchers.IO) { MDC.get(MdcKeys.CORR_ID) })
        Unit
      }
    }

    SourceHarvesterExecutor(harvester, sourceRepository, repositoryRepository).refreshSubscriptions()

    assertThat(seen).hasSize(2).allMatch { it != null && it.matches(Regex("[a-zA-Z0-9]{4}/[a-zA-Z0-9]{4}")) }
    assertThat(seen.map { it!!.substringBefore('/') }.distinct()).hasSize(1)
    assertThat(seen.distinct()).hasSize(2)
    assertThat(MDC.get(MdcKeys.CORR_ID)).isNull()
  }

  @Test
  fun `at most 10 sources harvest at once`() {
    val sourceRepository = mock<SourceRepository> { on { findAllDueForHarvest(any(), any()) } doReturn sources(30) }
    val repositoryRepository = mock<RepositoryRepository> { on { findById(any()) } doReturn repository }
    val running = AtomicInteger()
    val peak = AtomicInteger()
    val harvester = mock<RepositoryHarvester> {
      onBlocking { harvestScheduled(any()) } doSuspendableAnswer {
        peak.accumulateAndGet(running.incrementAndGet()) { a, b -> maxOf(a, b) }
        delay(20)
        running.decrementAndGet()
        Unit
      }
    }

    SourceHarvesterExecutor(harvester, sourceRepository, repositoryRepository).refreshSubscriptions()

    assertThat(peak.get()).isBetween(1, 10)
  }

  @Test
  fun `a source whose repository is gone is skipped`() {
    val sourceRepository = mock<SourceRepository> { on { findAllDueForHarvest(any(), any()) } doReturn sources(1) }
    val repositoryRepository = mock<RepositoryRepository> { on { findById(any()) } doReturn null }
    val calls = AtomicInteger()
    val harvester = mock<RepositoryHarvester> {
      onBlocking { harvestScheduled(any()) } doSuspendableAnswer { calls.incrementAndGet(); Unit }
    }

    SourceHarvesterExecutor(harvester, sourceRepository, repositoryRepository).refreshSubscriptions()

    assertThat(calls.get()).isZero()
  }
}
