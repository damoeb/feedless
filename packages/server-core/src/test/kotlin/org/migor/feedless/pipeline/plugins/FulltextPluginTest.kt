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
import org.migor.feedless.common.HttpResponse
import org.migor.feedless.common.LocaleProperties
import org.migor.feedless.document.Document
import org.migor.feedless.document.ReleaseStatus
import org.migor.feedless.text.datetime.DateTimeExtractor
import org.migor.feedless.feed.parser.json.JsonItem
import org.migor.feedless.generated.types.FetchActionDebugResponse
import org.migor.feedless.repository.Repository
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.scrape.HttpFetchOutput
import org.migor.feedless.scrape.LogCollector
import org.migor.feedless.scrape.ScrapeActionOutput
import org.migor.feedless.scrape.ScrapeOutput
import org.migor.feedless.scrape.ScrapeService
import org.migor.feedless.scrape.WebToArticleTransformer
import org.migor.feedless.source.Source
import org.migor.feedless.source.SourceRepository
import org.migor.feedless.source.SourceUseCase
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Spy
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness
import java.time.LocalDateTime
import java.util.Locale

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FulltextPluginTest {

  @Mock
  lateinit var sourceUseCase: SourceUseCase

  @Mock
  lateinit var scrapeService: ScrapeService

  @Mock
  lateinit var sourceRepository: SourceRepository

  @Mock
  lateinit var webToArticleTransformer: WebToArticleTransformer

  @Mock
  lateinit var localeProperties: LocaleProperties

  @Spy
  var dateTimeExtractor = DateTimeExtractor()

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
  fun `given readability, mapEntity returns the article and logs the title change`() = runTest {
    givenArticle(html = "<html/>", text = "body")
    val logCollector = LogCollector()

    val actual = fulltextPlugin.mapEntity(
      document = document(startingAt = null),
      repository = mock(Repository::class.java),
      params = FulltextPluginParams(readability = true, summary = false, inheritParams = false),
      logCollector = logCollector
    )

    assertThat(actual.title).isEqualTo("after")
    assertThat(actual.html).isEqualTo("<p>body</p>")
    assertThat(logCollector.logs.map { it.message }).containsExactly(
      "title 'before' -> 'after'",
      "text '' (0 chars) -> 'body' (4 chars)",
    )
  }

  @Test
  fun `given an event, mapEntity logs the datetime candidates of its body`() = runTest {
    givenArticle(html = "<html lang=\"de\"/>", text = "Konzert am 27. September 2024, 20:15 Uhr")
    val logCollector = LogCollector()

    fulltextPlugin.mapEntity(
      document = document(startingAt = LocalDateTime.of(2024, 9, 27, 20, 15)),
      repository = mock(Repository::class.java),
      paramsJson = """{"readability":true,"summary":false,"inheritParams":false}""",
      logCollector = logCollector
    )

    assertThat(logCollector.logs.map { it.message }).containsExactly(
      "title 'before' -> 'after'",
      "text '' (0 chars) -> 'Konzert am 27. September 2024, 20:15 Uhr' (40 chars)",
      "1 datetime candidate ['27. September 2024, 20:15'] vs startingAt 2024-09-27T20:15 -> confidence high",
    )
  }

  @Test
  fun `given an event and extractDates off, mapEntity logs no datetime candidates`() = runTest {
    givenArticle(html = "<html lang=\"de\"/>", text = "Konzert am 27.09.2024, 20:15 Uhr")
    val logCollector = LogCollector()

    fulltextPlugin.mapEntity(
      document = document(startingAt = LocalDateTime.of(2024, 9, 27, 20, 15)),
      repository = mock(Repository::class.java),
      params = FulltextPluginParams(readability = true, summary = false, inheritParams = false, extractDates = false),
      logCollector = logCollector
    )

    assertThat(logCollector.logs.map { it.message }).containsExactly(
      "title 'before' -> 'after'",
      "text '' (0 chars) -> 'Konzert am 27.09.2024, 20:15 Uhr' (32 chars)",
    )
  }

  @Test
  fun `given no html lang, mapEntity reads the body in the default locale`() = runTest {
    `when`(localeProperties.defaultLocale).thenReturn(Locale.GERMAN)
    givenArticle(html = "<html/>", text = "Konzert am 27. September 2024")
    val logCollector = LogCollector()

    fulltextPlugin.mapEntity(
      document = document(startingAt = LocalDateTime.of(2024, 9, 27, 20, 15)),
      repository = mock(Repository::class.java),
      params = FulltextPluginParams(readability = true, summary = false, inheritParams = false),
      logCollector = logCollector
    )

    assertThat(logCollector.logs.last().message)
      .isEqualTo("1 datetime candidate ['27. September 2024'] vs startingAt 2024-09-27T20:15 -> confidence medium")
  }

  @Test
  fun `given a long body, mapEntity logs its transformation abbreviated`() = runTest {
    givenArticle(html = "<html/>", text = "a".repeat(100))
    val logCollector = LogCollector()

    fulltextPlugin.mapEntity(
      document = document(startingAt = null).copy(text = "teaser\n  text"),
      repository = mock(Repository::class.java),
      params = FulltextPluginParams(readability = true, summary = false, inheritParams = false),
      logCollector = logCollector
    )

    assertThat(logCollector.logs.last().message)
      .isEqualTo("text 'teaser text' (13 chars) -> '${"a".repeat(80)}…' (100 chars)")
  }

  @Test
  fun `given the date is only in the original title, mapEntity rates it with its end time`() = runTest {
    givenArticle(html = "<html lang=\"de\"/>", text = "Referat Mobbing und Ausgrenzung")
    val logCollector = LogCollector()

    fulltextPlugin.mapEntity(
      document = document(
        startingAt = LocalDateTime.of(2026, 9, 24, 8, 0),
        title = "Referat 24. September 2026, 19:30 bis 21:00 Uhr"
      ),
      repository = mock(Repository::class.java),
      params = FulltextPluginParams(readability = true, summary = false, inheritParams = false),
      logCollector = logCollector
    )

    assertThat(logCollector.logs.map { it.message }.last()).isEqualTo(
      "1 datetime candidate ['24. September 2026, 19:30'] vs startingAt 2026-09-24T08:00 " +
        "-> confidence medium, range 19:30-21:00"
    )
  }

  @Test
  fun `given a standalone time apart from any date, mapEntity logs it`() = runTest {
    givenArticle(html = "<html lang=\"de\"/>", text = "Konzert am 27. September 2024. Türöffnung ab 19:00 Uhr")
    val logCollector = LogCollector()

    fulltextPlugin.mapEntity(
      document = document(startingAt = LocalDateTime.of(2024, 9, 27, 8, 0)),
      repository = mock(Repository::class.java),
      params = FulltextPluginParams(readability = true, summary = false, inheritParams = false),
      logCollector = logCollector
    )

    assertThat(logCollector.logs.map { it.message }.last()).isEqualTo("1 standalone time ['19:00']")
  }

  private suspend fun givenArticle(html: String, text: String) {
    val fetch = HttpFetchOutput(
      response = HttpResponse("text/html", "https://example.org/a", 200, html.toByteArray()),
      debug = mock(FetchActionDebugResponse::class.java),
    )
    `when`(scrapeService.scrape(any2(), any2()))
      .thenReturn(ScrapeOutput(outputs = listOf(ScrapeActionOutput(index = 0, fetch = fetch)), time = 0))
    val article = JsonItem()
    article.title = "after"
    article.html = "<p>body</p>"
    article.text = text
    `when`(webToArticleTransformer.fromHtml(any2(), any2(), anyBoolean())).thenReturn(article)
  }

  private fun document(startingAt: LocalDateTime?, title: String = "before") = Document(
    url = "https://example.org/a",
    title = title,
    text = "",
    contentHash = "",
    repositoryId = RepositoryId(),
    status = ReleaseStatus.unreleased,
    startingAt = startingAt,
  )

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
