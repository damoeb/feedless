package org.migor.feedless.service

import com.linecorp.kotlinjdsl.support.spring.data.jpa.repository.KotlinJdslJpqlExecutor
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.AppProfiles
import org.migor.feedless.actions.ClickPositionActionEntity
import org.migor.feedless.actions.ExtractBoundingBoxActionEntity
import org.migor.feedless.actions.ExtractEmit
import org.migor.feedless.actions.ExtractXpathActionEntity
import org.migor.feedless.actions.FetchActionEntity
import org.migor.feedless.actions.ScrapeActionEntity
import org.migor.feedless.agent.AgentService
import org.migor.feedless.common.HttpResponse
import org.migor.feedless.common.HttpService
import org.migor.feedless.data.jpa.models.SourceEntity
import org.migor.feedless.document.any
import org.migor.feedless.license.LicenseService
import org.migor.feedless.secrets.UserSecretService
import org.migor.feedless.util.CryptUtil.newCorrId
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.MockBeans
import org.springframework.test.context.ActiveProfiles
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

@SpringBootTest
@ActiveProfiles(profiles = ["test", AppProfiles.scrape])
@MockBeans(value = [
  MockBean(UserSecretService::class),
  MockBean(AgentService::class),
  MockBean(LicenseService::class),
  MockBean(HttpService::class),
  MockBean(KotlinJdslJpqlExecutor::class),
])
class ScrapeServiceTest {

  private val corrId: String = newCorrId()

  @Autowired
  lateinit var scrapeService: ScrapeService

  @Autowired
  lateinit var httpService: HttpService

  @BeforeEach
  fun setUp() {
  }

  @Test
  fun `does not prerender by default`() {
    assertThat(needsPrerendering(sourceWithActions(listOf()), 0)).isFalse()
  }

  @Test
  fun `prerendering respects current or next actions`() {
    val action = ClickPositionActionEntity()
    assertThat(needsPrerendering(sourceWithActions(listOf(action)), 1)).isFalse()
  }

  @Test
  fun `prerenders with click-position action`() {
    val action = ClickPositionActionEntity()
    assertThat(needsPrerendering(sourceWithActions(listOf(action)), 0)).isTrue()
  }

  @Test
  fun `prerenders with extract-box action`() {
    val action = ExtractBoundingBoxActionEntity()
    assertThat(needsPrerendering(sourceWithActions(listOf(action)), 0)).isTrue()
  }

  @Test
  fun `prerenders with pixel-extract action`() {
    val action = mock(ExtractXpathActionEntity::class.java)
    `when`(action.emit).thenReturn(arrayOf(ExtractEmit.pixel))
    assertThat(needsPrerendering(sourceWithActions(listOf(action)), 0)).isTrue()
  }

  @Test
  fun `prerenders with forcePrerender is true`() {
    val action = mock(FetchActionEntity::class.java)
    `when`(action.forcePrerender).thenReturn(true)
    assertThat(needsPrerendering(sourceWithActions(listOf(action)), 0)).isTrue()
  }

//  @Test
//  fun `fetch action`() {
//    val httpResponse = HttpResponse("text/html", "url", 200, "data".toByteArray())
//    `when`(httpService.httpGetCaching(anyString(), anyString(), anyInt(), ArgumentMatchers.any<Map<String, Any>>())).thenReturn(httpResponse)
//
//    val action = mock(FetchActionEntity::class.java)
//    scrapeService.scrape(corrId, sourceWithActions(listOf(action))).block()
//  }
//
//  @Test
//  fun `header action`() {
//  }
//
//  @Test
//  fun `purge action`() {
//  }
//
//  @Test
//  fun `click-xpath action`() {
//  }
//
//  @Test
//  fun `extract action`() {
//  }
//
//  @Test
//  fun `plugin action`() {
//  }

  private fun sourceWithActions(actions: List<ScrapeActionEntity>): SourceEntity {
    val source = mock(SourceEntity::class.java)
    `when`(source.actions).thenReturn(actions.toMutableList())
    return source
  }
}
