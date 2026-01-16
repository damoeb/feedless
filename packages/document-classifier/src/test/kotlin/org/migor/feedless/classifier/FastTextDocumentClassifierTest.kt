package org.migor.feedless.classifier

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.document.Document
import org.migor.feedless.document.DocumentClassifierModel
import org.migor.feedless.document.ReleaseStatus
import org.migor.feedless.repository.RepositoryId
import java.util.*

class FastTextDocumentClassifierTest {

  private lateinit var classifier: FastTextDocumentClassifier

  @BeforeEach
  fun setUp() {
    classifier = FastTextDocumentClassifier()
  }

  @Test
  fun `should classify document`() = runTest {
    val document = Document(
      url = "https://example.com/article",
      title = "New MacBook Pro Review",
      text = "The new MacBook Pro has incredible performance and battery life. The M3 chip delivers amazing speed.",
      repositoryId = RepositoryId(),
      status = ReleaseStatus.released,
      contentHash = UUID.randomUUID().toString()
    )

    val results = classifier.classify(document, DocumentClassifierModel("test"))

    assertThat(results).isNotEmpty()
    assertThat(results.first().category).isNotEmpty()
    assertThat(results.first().probability).isBetween(0.0, 1.0)
  }

}
