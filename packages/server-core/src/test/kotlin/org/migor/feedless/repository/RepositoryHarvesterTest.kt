package org.migor.feedless.repository

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.migor.feedless.ResumableHarvestException
import org.migor.feedless.data.jpa.enums.ProductCategory
import org.migor.feedless.document.DocumentService
import org.migor.feedless.service.LogCollector
import org.migor.feedless.service.ScrapeService
import org.migor.feedless.source.SourceDAO
import org.migor.feedless.source.SourceEntity
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
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
  lateinit var harvestDAO: HarvestDAO

  @Mock
  lateinit var repositoryDAO: RepositoryDAO

  @Mock
  lateinit var repositoryService: RepositoryService

  @Mock
  lateinit var scrapeService: ScrapeService

  @InjectMocks
  lateinit var repositoryHarvester: RepositoryHarvester

  private lateinit var repository: RepositoryEntity
  private lateinit var source: SourceEntity

  @BeforeEach
  fun setUp() = runTest {
    `when`(meterRegistry.counter(any(String::class.java), anyList())).thenReturn(mock(Counter::class.java))
    `when`(meterRegistry.counter(any(String::class.java))).thenReturn(mock(Counter::class.java))

    source = mock(SourceEntity::class.java)
    `when`(source.disabled).thenReturn(false)
    `when`(source.id).thenReturn(UUID.randomUUID())
    `when`(source.errorsInSuccession).thenReturn(0)

    repository = mock(RepositoryEntity::class.java)
    `when`(repository.id).thenReturn(UUID.randomUUID())
    `when`(repository.sourcesSyncCron).thenReturn("")
    `when`(repository.ownerId).thenReturn(UUID.randomUUID())
    `when`(repository.product).thenReturn(ProductCategory.feedless)


    `when`(sourceDAO.findAllByRepositoryIdOrderByCreatedAtDesc(any(UUID::class.java))).thenReturn(mutableListOf(source))


    `when`(repositoryDAO.findById(any(UUID::class.java))).thenReturn(Optional.of(repository))

    `when`(
      repositoryService.calculateScheduledNextAt(
        any(String::class.java), any(UUID::class.java), any(
          ProductCategory::class.java
        ), any(
          LocalDateTime::class.java
        )
      )
    ).thenReturn(Date())
  }

  @Test
  fun `given scrape fails will increment the error count`() = runTest {
    `when`(scrapeService.scrape(any(String::class.java), any(SourceEntity::class.java), any(LogCollector::class.java))).thenThrow(
      IllegalArgumentException("this is off")
    )
    `when`(source.errorsInSuccession).thenReturn(0)

    repositoryHarvester.handleRepository(corrId, repository.id)

    verify(scrapeService, times(1)).scrape(any(String::class.java), any(SourceEntity::class.java), any(LogCollector::class.java))

    verify(source).disabled = false
    verify(source).errorsInSuccession = 1
    verify(source).lastErrorMessage = "this is off"
    verify(sourceDAO, times(1)).save(source)
  }

  @Test
  fun `given scrape fails will disable source once error-count threshold is met`() = runTest {
    `when`(scrapeService.scrape(any(String::class.java), any(SourceEntity::class.java), any(LogCollector::class.java))).thenThrow(
      IllegalArgumentException("this is off")
    )
    `when`(source.errorsInSuccession).thenReturn(4)

    repositoryHarvester.handleRepository(corrId, repository.id)

    verify(scrapeService, times(1)).scrape(any(String::class.java), any(SourceEntity::class.java), any(LogCollector::class.java))

    verify(source).disabled = true
    verify(source).errorsInSuccession = 5
    verify(source).lastErrorMessage = "this is off"
    verify(sourceDAO, times(1)).save(source)
  }

  @Test
  fun `given scrape fails recoverable will not flag the source errornous`() = runTest {
    `when`(scrapeService.scrape(any(String::class.java), any(SourceEntity::class.java), any(LogCollector::class.java))).thenThrow(
      ResumableHarvestException(corrId, "", Duration.ofMinutes(5))
    )

    repositoryHarvester.handleRepository(corrId, repository.id)

    verify(scrapeService, times(1)).scrape(any(String::class.java), any(SourceEntity::class.java), any(LogCollector::class.java))
    verify(sourceDAO, times(0)).save(source)
  }

}
