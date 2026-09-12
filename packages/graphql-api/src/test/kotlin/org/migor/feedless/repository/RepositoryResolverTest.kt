package org.migor.feedless.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.migor.feedless.EntityVisibility
import org.migor.feedless.Vertical
import org.migor.feedless.actions.ClickPositionAction
import org.migor.feedless.actions.ClickXpathAction
import org.migor.feedless.actions.DomAction
import org.migor.feedless.actions.DomEventType
import org.migor.feedless.actions.ExecuteAction
import org.migor.feedless.actions.ExtractBoundingBoxAction
import org.migor.feedless.actions.ExtractXpathAction
import org.migor.feedless.actions.FetchAction
import org.migor.feedless.actions.HeaderAction
import org.migor.feedless.actions.PluginExecutionJson
import org.migor.feedless.actions.ScrapeActionId
import org.migor.feedless.api.mapper.toDto
import org.migor.feedless.generated.types.BoundingBox
import org.migor.feedless.generated.types.DOMActionSelect
import org.migor.feedless.generated.types.DOMActionType
import org.migor.feedless.generated.types.DOMElement
import org.migor.feedless.generated.types.DOMElementByNameOrXPath
import org.migor.feedless.generated.types.DOMElementByXPath
import org.migor.feedless.generated.types.DOMExtract
import org.migor.feedless.generated.types.FulltextPluginParams
import org.migor.feedless.generated.types.GeoPoint
import org.migor.feedless.generated.types.HttpFetch
import org.migor.feedless.generated.types.HttpGetRequest
import org.migor.feedless.generated.types.PluginExecution
import org.migor.feedless.generated.types.PluginExecutionParams
import org.migor.feedless.generated.types.RequestHeader
import org.migor.feedless.generated.types.Retention
import org.migor.feedless.generated.types.ScrapeAction
import org.migor.feedless.generated.types.ScrapeBoundingBox
import org.migor.feedless.generated.types.ScrapeEmit
import org.migor.feedless.generated.types.ScrapeExtract
import org.migor.feedless.generated.types.ScrapeFlow
import org.migor.feedless.generated.types.StringLiteralOrVariable
import org.migor.feedless.generated.types.ViewPort
import org.migor.feedless.generated.types.Visibility
import org.migor.feedless.generated.types.XYPosition
import org.migor.feedless.geo.LatLonPoint
import org.migor.feedless.group.GroupId
import org.migor.feedless.pipelineJob.MaxAgeDaysDateField
import org.migor.feedless.source.ExtractEmit
import org.migor.feedless.source.PuppeteerWaitUntil
import org.migor.feedless.source.Source
import org.migor.feedless.source.SourceId
import org.migor.feedless.user.UserId
import org.migor.feedless.util.toMillis
import java.time.LocalDateTime
import org.migor.feedless.generated.types.PuppeteerWaitUntil as PuppeteerWaitUntilDto
import org.migor.feedless.generated.types.Repository as RepositoryDto
import org.migor.feedless.generated.types.Source as SourceDto
import org.migor.feedless.generated.types.Vertical as VerticalDto

class RepositoryResolverTest {

  @Test
  fun testRepositoryToDto() {
    val repositoryId = RepositoryId()
    val ownerId = UserId()
    val title = "Test Repository"
    val description = "A test repository for unit testing"
    val tags = arrayOf("test", "automation", "kotlin")
    val visibility = EntityVisibility.isPublic
    val sourcesSyncCron = "0 0 * * *"
    val retentionMaxCapacity = 100
    val retentionMaxAgeDays = 30
    val pushNotificationsEnabled = true
    val lastUpdatedAt = LocalDateTime.of(2024, 11, 21, 12, 0, 0)
    val createdAt = LocalDateTime.of(2024, 11, 1, 10, 0, 0)
    val archived = false
    val pullsPerMonth = 500
    val product = Vertical.feedless
    val shareKey = "test-share-key-123"
    val triggerScheduledNextAt = LocalDateTime.of(2024, 11, 22, 12, 0, 0)

    val incoming = org.migor.feedless.repository.Repository(
      id = repositoryId,
      title = title,
      description = description,
      tags = tags,
      visibility = visibility,
      sourcesSyncCron = sourcesSyncCron,
      retentionMaxCapacity = retentionMaxCapacity,
      pushNotificationsEnabled = pushNotificationsEnabled,
      retentionMaxAgeDays = retentionMaxAgeDays,
      retentionMaxAgeDaysReferenceField = MaxAgeDaysDateField.createdAt,
      lastUpdatedAt = lastUpdatedAt,
      disabledFrom = null,
      shareKey = shareKey,
      sunsetAfterTimestamp = null,
      sunsetAfterTotalDocumentCount = null,
      documentCountSinceCreation = 0,
      archived = archived,
      product = product,
      triggerScheduledNextAt = triggerScheduledNextAt,
      schemaVersion = 0,
      pullsPerMonth = pullsPerMonth,
      lastPullSync = null,
      plugins = emptyList(),
      ownerId = ownerId,
      groupId = GroupId(),
      createdAt = createdAt,
    )

    val expected = RepositoryDto(
      id = repositoryId.uuid.toString(),
      ownerId = ownerId.uuid.toString(),
      title = title,
      description = description,
      tags = tags.toList(),
      visibility = Visibility.isPublic,
      refreshCron = sourcesSyncCron,
      retention = Retention(
        maxCapacity = retentionMaxCapacity,
        maxAgeDays = retentionMaxAgeDays
      ),
      shareKey = shareKey, // included because currentUserIsOwner = true
      product = VerticalDto.feedless,
      createdAt = createdAt.toMillis(),
      lastUpdatedAt = lastUpdatedAt.toMillis(),
      nextUpdateAt = triggerScheduledNextAt.toMillis(),
      disabledFrom = null,
      archived = archived,
      pushNotificationsEnabled = pushNotificationsEnabled,
      pullsPerMonth = pullsPerMonth,
      currentUserIsOwner = true,
      documentCount = 0L,
      sourcesCount = 0,
      sourcesCountWithProblems = 0,
      annotations = null,
      plugins = emptyList(),
    )

    assertThat(incoming.toDto(true)).isEqualTo(expected)
  }

  @Test
  fun testSourceToDto() {
    val sourceId = SourceId()
    val repositoryId = RepositoryId()
    val title = "Test Web Scraper Source"
    val language = "en"
    val tags = arrayOf("tech", "news", "automation")
    val disabled = false
    val lastRecordsRetrieved = 42
    val lastRefreshedAt = LocalDateTime.of(2024, 11, 20, 14, 30, 0)
    val errorsInSuccession = 0
    val lastErrorMessage: String? = null
    val createdAt = LocalDateTime.of(2024, 11, 1, 9, 0, 0)

    val incoming = Source(
      id = sourceId,
      language = language,
      title = title,
      latLon = LatLonPoint(12.0, 45.0),
      tags = tags,
      repositoryId = repositoryId,
      disabled = disabled,
      lastRecordsRetrieved = lastRecordsRetrieved,
      lastRefreshedAt = lastRefreshedAt,
      errorsInSuccession = errorsInSuccession,
      lastErrorMessage = lastErrorMessage,
      createdAt = createdAt,
      actions = listOf(
        // FetchAction - fetches a URL with browser emulation
        FetchAction(
          id = ScrapeActionId(),
          pos = 0,
          sourceId = sourceId,
          timeout = 30000,
          url = "https://example.com/news",
          language = "en",
          forcePrerender = true,
          isVariable = false,
          viewportWidth = 1920,
          viewportHeight = 1080,
          isMobile = false,
          isLandscape = true,
          waitUntil = PuppeteerWaitUntil.networkidle2,
          additionalWaitSec = 3,
          createdAt = createdAt
        ),
        // HeaderAction - sets custom HTTP header
        HeaderAction(
          id = ScrapeActionId(),
          pos = 1,
          sourceId = sourceId,
          name = "User-Agent",
          value = "FeedlessBot/1.0",
          createdAt = createdAt
        ),
        // DomAction click - clicks on cookie consent button
        DomAction(
          id = ScrapeActionId(),
          pos = 2,
          sourceId = sourceId,
          xpath = "//button[@id='accept-cookies']",
          event = DomEventType.click,
          data = null,
          createdAt = createdAt
        ),
        // DomAction type - enters search query
        DomAction(
          id = ScrapeActionId(),
          pos = 3,
          sourceId = sourceId,
          xpath = "//input[@name='search']",
          event = DomEventType.type,
          data = "kotlin news",
          createdAt = createdAt
        ),
        // DomAction select - selects dropdown option
        DomAction(
          id = ScrapeActionId(),
          pos = 4,
          sourceId = sourceId,
          xpath = "//select[@name='category']",
          event = DomEventType.select,
          data = "technology",
          createdAt = createdAt
        ),
        // DomAction purge - removes unwanted elements
        DomAction(
          id = ScrapeActionId(),
          pos = 5,
          sourceId = sourceId,
          xpath = "//div[@class='advertisement']",
          event = DomEventType.purge,
          data = null,
          createdAt = createdAt
        ),
        // ClickXpathAction - clicks element by xpath
        ClickXpathAction(
          id = ScrapeActionId(),
          pos = 6,
          sourceId = sourceId,
          xpath = "//button[@class='load-more']",
          createdAt = createdAt
        ),
        // ClickPositionAction - clicks at specific coordinates
        ClickPositionAction(
          id = ScrapeActionId(),
          pos = 7,
          sourceId = sourceId,
          x = 500,
          y = 300,
          createdAt = createdAt
        ),
        // ExtractXpathAction - extracts content by xpath
        ExtractXpathAction(
          id = ScrapeActionId(),
          pos = 8,
          sourceId = sourceId,
          fragmentName = "articles",
          xpath = "//article[@class='news-item']",
          emit = arrayOf(
            ExtractEmit.text,
            ExtractEmit.html,
            ExtractEmit.date
          ),
          uniqueBy = ExtractEmit.text,
          createdAt = createdAt
        ),
        // ExtractBoundingBoxAction - extracts content from screen region
        ExtractBoundingBoxAction(
          id = ScrapeActionId(),
          pos = 9,
          sourceId = sourceId,
          fragmentName = "screenshot_region",
          x = 100,
          y = 200,
          w = 800,
          h = 600,
          createdAt = createdAt
        ),
        // ExecuteAction - executes a plugin
        ExecuteAction(
          id = ScrapeActionId(),
          pos = 10,
          sourceId = sourceId,
          pluginId = "org_feedless_fulltext",
          executorParams = PluginExecutionJson(
            paramsJsonString = """{"readability":true,"summary":false,"inheritParams":false}"""
          ),
          createdAt = createdAt
        )
      )
    )

    val expected = SourceDto(
      id = sourceId.uuid.toString(),
      title = title,
      disabled = disabled,
      lastErrorMessage = lastErrorMessage,
      tags = tags.toList(),
      latLng = GeoPoint(
        lat = 12.0,
        lng = 45.0
      ),
      recordCount = 0,
      lastRecordsRetrieved = lastRecordsRetrieved,
      lastRefreshedAt = lastRefreshedAt.toMillis(),
      harvests = emptyList(),
      flow = ScrapeFlow(
        sequence = listOf(
          // FetchAction
          ScrapeAction(
            fetch = HttpFetch(
              get = HttpGetRequest(
                url = StringLiteralOrVariable(literal = "https://example.com/news"),
                timeout = 30000,
                viewport = ViewPort(
                  width = 1920,
                  height = 1080,
                  isMobile = false,
                  isLandscape = true
                ),
                forcePrerender = true,
                language = "en",
                waitUntil = PuppeteerWaitUntilDto.networkidle2,
                additionalWaitSec = 3
              )
            )
          ),
          // HeaderAction
          ScrapeAction(
            header = RequestHeader(
              name = "User-Agent",
              value = "FeedlessBot/1.0"
            )
          ),
          // DomAction click
          ScrapeAction(
            click = DOMElement(
              element = DOMElementByNameOrXPath(
                xpath = DOMElementByXPath(value = "//button[@id='accept-cookies']")
              )
            )
          ),
          // DomAction type
          ScrapeAction(
            type = DOMActionType(
              typeValue = "kotlin news",
              element = DOMElementByXPath(value = "//input[@name='search']")
            )
          ),
          // DomAction select
          ScrapeAction(
            select = DOMActionSelect(
              selectValue = "technology",
              element = DOMElementByXPath(value = "//select[@name='category']")
            )
          ),
          // DomAction purge
          ScrapeAction(
            purge = DOMElementByXPath(value = "//div[@class='advertisement']")
          ),
          // ClickXpathAction
          ScrapeAction(
            click = DOMElement(
              element = DOMElementByNameOrXPath(
                xpath = DOMElementByXPath(value = "//button[@class='load-more']")
              )
            )
          ),
          // ClickPositionAction
          ScrapeAction(
            click = DOMElement(
              position = XYPosition(x = 500, y = 300)
            )
          ),
          // ExtractXpathAction
          ScrapeAction(
            extract = ScrapeExtract(
              fragmentName = "articles",
              selectorBased = DOMExtract(
                fragmentName = "articles",
                xpath = DOMElementByXPath(value = "//article[@class='news-item']"),
                emit = listOf(ScrapeEmit.text, ScrapeEmit.html, ScrapeEmit.date),
                uniqueBy = ScrapeEmit.text
              )
            )
          ),
          // ExtractBoundingBoxAction
          ScrapeAction(
            extract = ScrapeExtract(
              fragmentName = "screenshot_region",
              imageBased = ScrapeBoundingBox(
                BoundingBox(x = 100, y = 200, w = 800, h = 600)
              )
            )
          ),
          // ExecuteAction
          ScrapeAction(
            execute = PluginExecution(
              pluginId = "org_feedless_fulltext",
              params = PluginExecutionParams(
                org_feedless_fulltext = FulltextPluginParams(
                  readability = true,
                  summary = false,
                  inheritParams = false
                )
              )
            )
          )
        )
      )
    )

    assertThat(incoming.toDto()).isEqualTo(expected)
  }
}
