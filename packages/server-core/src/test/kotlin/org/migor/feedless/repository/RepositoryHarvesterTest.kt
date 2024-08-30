package org.migor.feedless.repository

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.migor.feedless.ResumableHarvestException
import org.migor.feedless.data.jpa.enums.ProductCategory
import org.migor.feedless.document.DocumentService
import org.migor.feedless.document.any
import org.migor.feedless.document.anyList
import org.migor.feedless.document.anyOrNull
import org.migor.feedless.document.eq
import org.migor.feedless.service.ScrapeOutput
import org.migor.feedless.service.ScrapeService
import org.migor.feedless.source.SourceDAO
import org.migor.feedless.source.SourceEntity
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness
import java.time.Duration
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RepositoryHarvesterTest {

  private val corrId = "test"

  @Mock
  lateinit var sourceDAO: SourceDAO

  @Mock
  lateinit var meterRegistry: MeterRegistry

  @Mock
  lateinit var documentService: DocumentService

  @Mock
  lateinit var repositoryDAO: RepositoryDAO

  @Mock
  lateinit var repositoryService: RepositoryService

  @Mock
  lateinit var scrapeService: ScrapeService

  @InjectMocks
  lateinit var repositoryHarvester: RepositoryHarvester

  private lateinit var repository: RepositoryEntity

  @BeforeEach
  fun setUp() {
    `when`(meterRegistry.counter(any(String::class.java), anyList())).thenReturn(mock(Counter::class.java))
    `when`(meterRegistry.counter(any(String::class.java))).thenReturn(mock(Counter::class.java))

    val source = mock(SourceEntity::class.java)
    `when`(source.erroneous).thenReturn(false)
    `when`(source.id).thenReturn(UUID.randomUUID())

    repository = mock(RepositoryEntity::class.java)
    `when`(repository.sources).thenReturn(mutableListOf(source))
    `when`(repository.id).thenReturn(UUID.randomUUID())
    `when`(repository.sourcesSyncCron).thenReturn("")
    `when`(repository.ownerId).thenReturn(UUID.randomUUID())
    `when`(repository.product).thenReturn(ProductCategory.feedless)


    `when`(repositoryDAO.findById(any(UUID::class.java))).thenReturn(Optional.of(repository))

    `when`(repositoryService.calculateScheduledNextAt(any(String::class.java), any(UUID::class.java), any(
      ProductCategory::class.java), any(
      LocalDateTime::class.java))).thenReturn(Date())
  }

  @Test
  fun `given scrape fails will flag the source errornous`() {
    runBlocking {
      `when`(scrapeService.scrape(any(String::class.java), any(SourceEntity::class.java))).thenThrow(
        IllegalArgumentException("")
      )

      repositoryHarvester.handleRepository(corrId, repository.id)

      verify(scrapeService, times(1)).scrape(any(String::class.java), any(SourceEntity::class.java))
      verify(sourceDAO, times(1)).setErrorState(
        any(UUID::class.java),
        any(Boolean::class.java),
        anyOrNull(String::class.java)
      )
    }
  }

  @Test
  fun `given scrape fails recoverable will not flag the source errornous`() {
    runBlocking {
      `when`(scrapeService.scrape(any(String::class.java), any(SourceEntity::class.java))).thenThrow(
        ResumableHarvestException(corrId, "", Duration.ofMinutes(5))
      )

      repositoryHarvester.handleRepository(corrId, repository.id)

      verify(scrapeService, times(1)).scrape(any(String::class.java), any(SourceEntity::class.java))
      verifyNoInteractions(sourceDAO)
    }
  }

  @Test
  fun `handleRepository ignores errornous sources`() {
    runBlocking {
      `when`(scrapeService.scrape(any(String::class.java), any(SourceEntity::class.java)))
        .thenReturn(ScrapeOutput(
          outputs = emptyList(),
          logs = emptyList(),
          time = 0
        ))

      val sourceNonErrornous = mock(SourceEntity::class.java)
      `when`(sourceNonErrornous.erroneous).thenReturn(false)
      `when`(sourceNonErrornous.id).thenReturn(UUID.randomUUID())

      val sourceErrornous = mock(SourceEntity::class.java)
      `when`(sourceErrornous.erroneous).thenReturn(true)
      `when`(sourceErrornous.id).thenReturn(UUID.randomUUID())

      `when`(repository.sources).thenReturn(mutableListOf(sourceNonErrornous, sourceErrornous))

      repositoryHarvester.handleRepository(corrId, repository.id)

      verify(scrapeService, times(1)).scrape(any(String::class.java), eq(sourceNonErrornous))
      verifyNoMoreInteractions(scrapeService)
    }
  }
}
