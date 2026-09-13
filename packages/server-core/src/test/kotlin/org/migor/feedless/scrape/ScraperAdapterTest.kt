package org.migor.feedless.scrape

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.migor.feedless.feed.parser.json.JsonItem
import org.migor.feedless.generated.types.MimeData
import org.migor.feedless.generated.types.ScrapeExtractFragment
import org.migor.feedless.generated.types.ScrapeExtractFragmentPart
import org.migor.feedless.generated.types.TextData
import org.migor.feedless.pipeline.FragmentOutput
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.source.Source
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class ScraperAdapterTest {

  private val scrapeService = mock(ScrapeService::class.java)
  private val source = Source(title = "source", repositoryId = RepositoryId(), actions = emptyList())
  private val logCollector = LogCollector()

  @Test
  fun `maps the action count and the last action's fragment field by field`() = runTest {
    val item = JsonItem()
    `when`(scrapeService.scrape(source, logCollector)).thenReturn(
      ScrapeOutput(
        outputs = listOf(
          ScrapeActionOutput(index = 0, fragment = FragmentOutput(fragmentName = "first", items = listOf(JsonItem()))),
          ScrapeActionOutput(
            index = 1,
            fragment = FragmentOutput(
              fragmentName = "last",
              items = listOf(item),
              fragments = listOf(
                ScrapeExtractFragment(
                  html = TextData("<p>p</p>"),
                  text = TextData("p"),
                  data = MimeData(mimeType = ScrapeMimeTypes.MIME_URL, data = "https://example.org/2"),
                  uniqueBy = ScrapeExtractFragmentPart.data,
                ),
                ScrapeExtractFragment(html = TextData("<b/>"), uniqueBy = ScrapeExtractFragmentPart.html),
                ScrapeExtractFragment(text = TextData("t"), uniqueBy = ScrapeExtractFragmentPart.text),
              ),
            ),
          ),
        ),
        time = 0,
      )
    )

    val result = ScraperAdapter(scrapeService).scrape(source, logCollector)

    assertThat(result).isEqualTo(
      ScrapeResult(
        actionCount = 2,
        lastFragment = ScrapedFragmentOutput(
          items = listOf(item),
          fragments = listOf(
            ScrapedFragment(
              html = "<p>p</p>",
              text = "p",
              data = ScrapedData(mimeType = ScrapeMimeTypes.MIME_URL, data = "https://example.org/2"),
              uniqueBy = ScrapedFragmentPart.data,
            ),
            ScrapedFragment(html = "<b/>", uniqueBy = ScrapedFragmentPart.html),
            ScrapedFragment(text = "t", uniqueBy = ScrapedFragmentPart.text),
          ),
        ),
      )
    )
  }

  @Test
  fun `a scrape without outputs has no actions and no fragment`() = runTest {
    `when`(scrapeService.scrape(source, logCollector)).thenReturn(ScrapeOutput(outputs = emptyList(), time = 0))

    val result = ScraperAdapter(scrapeService).scrape(source, logCollector)

    assertThat(result).isEqualTo(ScrapeResult(actionCount = 0, lastFragment = null))
  }

  @Test
  fun `a last action without a fragment still counts as an action`() = runTest {
    `when`(scrapeService.scrape(source, logCollector)).thenReturn(
      ScrapeOutput(outputs = listOf(ScrapeActionOutput(index = 0)), time = 0)
    )

    val result = ScraperAdapter(scrapeService).scrape(source, logCollector)

    assertThat(result).isEqualTo(ScrapeResult(actionCount = 1, lastFragment = null))
  }
}
