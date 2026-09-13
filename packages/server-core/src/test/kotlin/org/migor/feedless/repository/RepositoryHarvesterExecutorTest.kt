package org.migor.feedless.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.migor.feedless.capability.MdcKeys
import org.migor.feedless.group.GroupId
import org.migor.feedless.user.UserId
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doSuspendableAnswer
import org.mockito.kotlin.mock
import org.slf4j.MDC
import org.springframework.scheduling.annotation.Scheduled
import java.util.Collections

class RepositoryHarvesterExecutorTest {

  @Test
  fun `verify refreshSubscriptions is annotated with scheduled`() {
    val method = RepositoryHarvesterExecutor::class.java.declaredMethods.first { it.name == "refreshSubscriptions" }
    assertThat(method.getAnnotation(Scheduled::class.java)).isNotNull()
  }

  @Test
  fun `each harvest of a run logs under a child of the run's correlation id`() {
    MDC.clear()
    val repos = listOf(
      Repository(title = "a", ownerId = UserId(), groupId = GroupId()),
      Repository(title = "b", ownerId = UserId(), groupId = GroupId()),
    )
    val repositoryRepository = mock<RepositoryRepository> {
      on { findAllWhereNextHarvestIsDue(any(), any()) } doReturn repos
    }
    val seen = Collections.synchronizedList(mutableListOf<String?>())
    val repositoryHarvester = mock<RepositoryHarvester> {
      onBlocking { harvestRepository(any()) } doSuspendableAnswer {
        seen.add(withContext(Dispatchers.IO) { MDC.get(MdcKeys.CORR_ID) })
        Unit
      }
    }

    RepositoryHarvesterExecutor(repositoryHarvester, repositoryRepository).refreshSubscriptions()

    assertThat(seen).hasSize(2).allMatch { it != null && it.matches(Regex("[a-zA-Z0-9]{4}/[a-zA-Z0-9]{4}")) }
    assertThat(seen.map { it!!.substringBefore('/') }.distinct()).hasSize(1)
    assertThat(seen.distinct()).hasSize(2)
    assertThat(MDC.get(MdcKeys.CORR_ID)).isNull()
  }
}
