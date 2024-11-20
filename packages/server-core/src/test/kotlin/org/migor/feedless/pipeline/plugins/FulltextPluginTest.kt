package org.migor.feedless.pipeline.plugins

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.migor.feedless.actions.ClickPositionActionEntity
import org.migor.feedless.actions.ClickXpathActionEntity
import org.migor.feedless.actions.DomActionEntity
import org.migor.feedless.actions.ExecuteActionEntity
import org.migor.feedless.actions.ExtractBoundingBoxActionEntity
import org.migor.feedless.actions.ExtractXpathActionEntity
import org.migor.feedless.actions.FetchActionEntity
import org.migor.feedless.actions.HeaderActionEntity
import org.migor.feedless.actions.PluginExecutionJsonEntity
import org.migor.feedless.actions.ScrapeActionEntity
import org.migor.feedless.actions.WaitActionEntity
import org.migor.feedless.document.DocumentEntity
import org.migor.feedless.generated.types.FulltextPluginParamsInput
import org.migor.feedless.generated.types.PluginExecutionParamsInput
import org.migor.feedless.repository.RepositoryEntity
import org.migor.feedless.repository.any
import org.migor.feedless.scrape.LogCollector
import org.migor.feedless.scrape.ScrapeOutput
import org.migor.feedless.scrape.ScrapeService
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

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FulltextPluginTest {

  @Mock
  lateinit var scrapeService: ScrapeService

  @InjectMocks
  lateinit var fulltextPlugin: FulltextPlugin

  @BeforeEach
  fun setUp() {
  }

  @Test
  fun `mapEntity calls scrape`() = runTest {
    val source = mock(SourceEntity::class.java)
    val document = mock(DocumentEntity::class.java)
    `when`(document.url).thenReturn("https://example.org")
    `when`(document.source).thenReturn(source)

    val repository = mock(RepositoryEntity::class.java)
    `when`(repository.sources).thenReturn(mutableListOf(source))

    val params = PluginExecutionJsonEntity(
      org_feedless_fulltext = FulltextPluginParamsInput(
        readability = true,
        summary = false,
        inheritParams = true
      )
    )

    `when`(
      scrapeService.scrape(
        any(SourceEntity::class.java),
        any(LogCollector::class.java)
      )
    ).thenReturn(
      ScrapeOutput(
        outputs = emptyList(),
        time = 0,
      )
    )

    val response =
      fulltextPlugin.mapEntity(
        document = document,
        repository = repository,
        params = params,
        logCollector = LogCollector()
      )

    assertThat(response).isNotNull
    verify(scrapeService, times(1)).scrape(
      any(SourceEntity::class.java),
      any(LogCollector::class.java)
    )
  }

  @Test
  fun `given source actions is empty, merge returns fetchAction param`() = runTest {
    val fetchAction = mock(FetchActionEntity::class.java)
    val sourceActions = listOf<ScrapeActionEntity>()

    assertThat(fulltextPlugin.mergeWithSourceActions(fetchAction, sourceActions)).isEqualTo(listOf(fetchAction))
  }

  @Test
  fun `given source actions contains fetchAction, merge returns fetchAction param`() = runTest {
    val fetchAction = mock(FetchActionEntity::class.java)
    val sourceActions = listOf<ScrapeActionEntity>(mock(FetchActionEntity::class.java))

    assertThat(fulltextPlugin.mergeWithSourceActions(fetchAction, sourceActions)).isEqualTo(listOf(fetchAction))
  }

  @Test
  fun `given source actions, merge returns actions without extract and execute`() = runTest {
    val fetchAction = mock(FetchActionEntity::class.java)
    val sourceFetch = mock(FetchActionEntity::class.java)
    val clickPosition = mock(ClickPositionActionEntity::class.java)
    val clickXpath = mock(ClickXpathActionEntity::class.java)
    val domAction = mock(DomActionEntity::class.java)
    val executeAction = mock(ExecuteActionEntity::class.java)
    val extractBbox = mock(ExtractBoundingBoxActionEntity::class.java)
    val extractXpath = mock(ExtractXpathActionEntity::class.java)
    val headerAction = mock(HeaderActionEntity::class.java)
    val waitAction = mock(WaitActionEntity::class.java)

    val sourceActions = listOf<ScrapeActionEntity>(
      headerAction,
      sourceFetch,
      clickPosition,
      clickXpath,
      domAction,
      executeAction,
      extractBbox,
      extractXpath,
      waitAction,
    )

    assertThat(fulltextPlugin.mergeWithSourceActions(fetchAction, sourceActions))
      .isEqualTo(
        listOf(
          headerAction,
          fetchAction,
          clickPosition,
          clickXpath,
          domAction,
          waitAction,
        )
      )
  }
}
