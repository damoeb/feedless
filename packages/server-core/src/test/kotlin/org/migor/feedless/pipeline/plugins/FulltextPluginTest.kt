package org.migor.feedless.pipeline.plugins

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.migor.feedless.actions.ClickPositionAction
import org.migor.feedless.actions.ClickXpathAction
import org.migor.feedless.actions.DomAction
import org.migor.feedless.actions.ExecuteAction
import org.migor.feedless.actions.ExtractBoundingBoxAction
import org.migor.feedless.actions.ExtractXpathAction
import org.migor.feedless.actions.FetchAction
import org.migor.feedless.actions.HeaderAction
import org.migor.feedless.actions.ScrapeAction
import org.migor.feedless.actions.WaitAction
import org.migor.feedless.any2
import org.migor.feedless.document.Document
import org.migor.feedless.repository.Repository
import org.migor.feedless.scrape.LogCollector
import org.migor.feedless.scrape.ScrapeOutput
import org.migor.feedless.scrape.ScrapeService
import org.migor.feedless.source.Source
import org.migor.feedless.source.SourceService
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
    lateinit var sourceService: SourceService

    @Mock
    lateinit var scrapeService: ScrapeService

    @InjectMocks
    lateinit var fulltextPlugin: FulltextPlugin

    @BeforeEach
    fun setUp() {
    }

    @Test
    fun `mapEntity calls scrape`() = runTest {
        val source = mock(Source::class.java)
        val document = mock(Document::class.java)
        `when`(document.url).thenReturn("https://example.org")
//       TODO `when`(document.source).thenReturn(source)

        val repository = mock(Repository::class.java)
//        TODO`when`(repository.sources).thenReturn(mutableListOf(source))

        val params = FulltextPluginParams(
            readability = true,
            summary = false,
            inheritParams = true
        )
        `when`(
            scrapeService.scrape(
                any2(),
                any2()
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
            any2(),
            any2()
        )
    }

    @Test
    fun `given source actions is empty, merge returns fetchAction param`() = runTest {
        val fetchAction = mock(FetchAction::class.java)
        val sourceActions = listOf<ScrapeAction>()

        assertThat(fulltextPlugin.mergeWithSourceActions(fetchAction, sourceActions)).isEqualTo(listOf(fetchAction))
    }

    @Test
    fun `given source actions contains fetchAction, merge returns fetchAction param`() = runTest {
        val fetchAction = mock(FetchAction::class.java)
        val sourceActions = listOf<ScrapeAction>(mock(FetchAction::class.java))

        assertThat(fulltextPlugin.mergeWithSourceActions(fetchAction, sourceActions)).isEqualTo(listOf(fetchAction))
    }

    @Test
    fun `given source actions, merge returns actions without extract and execute`() = runTest {
        val fetchAction = mock(FetchAction::class.java)
        val sourceFetch = mock(FetchAction::class.java)
        val clickPosition = mock(ClickPositionAction::class.java)
        val clickXpath = mock(ClickXpathAction::class.java)
        val domAction = mock(DomAction::class.java)
        val executeAction = mock(ExecuteAction::class.java)
        val extractBbox = mock(ExtractBoundingBoxAction::class.java)
        val extractXpath = mock(ExtractXpathAction::class.java)
        val headerAction = mock(HeaderAction::class.java)
        val waitAction = mock(WaitAction::class.java)

        val sourceActions = listOf<ScrapeAction>(
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
