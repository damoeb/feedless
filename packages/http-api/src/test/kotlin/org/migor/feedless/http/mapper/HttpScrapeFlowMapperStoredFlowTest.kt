package org.migor.feedless.http.mapper

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.migor.feedless.http.api.model.DomActionType
import org.migor.feedless.http.api.model.DomElementByXPath
import org.migor.feedless.http.api.model.HttpFetch
import org.migor.feedless.http.api.model.HttpGetRequest
import org.migor.feedless.http.api.model.RequestHeader
import org.migor.feedless.http.api.model.ScrapeAction
import org.migor.feedless.http.api.model.ScrapeFlow
import org.migor.feedless.http.api.model.StringLiteralOrVariable

class HttpScrapeFlowMapperStoredFlowTest {

  private val mapper = HttpScrapeFlowMapper()

  @Test
  fun `a stored flow maps back to the actions of the flow it was stored from`() {
    val flow = ScrapeFlow(
      sequence = listOf(
        ScrapeAction(header = RequestHeader(name = "Accept-Language", value = "de")),
        ScrapeAction(
          fetch = HttpFetch(
            get = HttpGetRequest(url = StringLiteralOrVariable(literal = "https://example.org"), forcePrerender = true),
          ),
        ),
        ScrapeAction(type = DomActionType(element = DomElementByXPath(value = "//input"), typeValue = "query")),
        ScrapeAction(purge = DomElementByXPath(value = "//footer")),
      ),
    )

    val restored = mapper.storedFlowToDomainActions(mapper.toStoredFlow(flow))

    // Domain actions carry fresh ids; compare what they mean, via the HTTP form.
    assert(mapper.toHttpFlow(restored) == mapper.toHttpFlow(mapper.toDomainActions(flow))) {
      "${mapper.toHttpFlow(restored)}"
    }
  }

  @Test
  fun `an invalid flow is rejected like it is on PATCH`() {
    val ambiguous = ScrapeFlow(
      sequence = listOf(
        ScrapeAction(
          fetch = HttpFetch(get = HttpGetRequest(url = StringLiteralOrVariable(literal = "https://example.org"))),
          purge = DomElementByXPath(value = "//div"),
        ),
      ),
    )

    val ex = assertThrows<IllegalArgumentException> { mapper.toStoredFlow(ambiguous) }
    assert(ex.message!!.contains("exactly one is allowed")) { ex.message!! }
  }
}
