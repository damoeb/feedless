package org.migor.feedless.analytics

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AnalyticsServiceTest {

  lateinit var service: AnalyticsService

  @BeforeEach
  fun setUp() {
    service = AnalyticsService()
    service.plausibleUrl = ""
    service.plausibleApiKey = ""
    service.plausibleSite = ""
  }

  @Test
  fun testParseStatsResponse() = runTest {
    val response = """
      {
        "results": [
          {
            "date": "2020-08-01",
            "visitors": 36085
          },
          {
            "date": "2020-09-01",
            "visitors": 27688
          },
          {
            "date": "2020-10-01",
            "visitors": 71615
          },
          {
            "date": "2020-11-01",
            "visitors": 31440
          },
          {
            "date": "2020-12-01",
            "visitors": 35804
          },
          {
            "date": "2021-01-01",
            "visitors": 0
          }
        ]
      }
    """.trimIndent()

    assertThat(service.parseStatsResponse(response)).isEqualTo(202632)
  }
}
