package org.migor.feedless.pipeline.plugins

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.migor.feedless.data.jpa.models.SourceSubscriptionEntity
import org.migor.feedless.data.jpa.models.WebDocumentEntity
import org.migor.feedless.generated.types.PluginExecutionParamsInput
import org.migor.feedless.service.ScrapeService
import org.migor.feedless.web.WebToArticleTransformer
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FulltextPluginTest {

  @Mock
  lateinit var webToArticleTransformer: WebToArticleTransformer

  @Mock
  lateinit var scrapeService: ScrapeService

  @InjectMocks
  lateinit var fulltextPlugin: FulltextPlugin

  @BeforeEach
  fun setUp() {
  }

  @Test
  @Disabled
  fun mapEntity() {
    val corrId = "test"
    val webDocument = Mockito.mock(WebDocumentEntity::class.java)
    val subscription = Mockito.mock(SourceSubscriptionEntity::class.java)
    val params = PluginExecutionParamsInput.newBuilder()

      .build()
    fulltextPlugin.mapEntity(corrId = corrId, webDocument = webDocument, subscription = subscription, params = params)
  }
}
