package org.migor.feedless.classifier

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.document.Document
import org.migor.feedless.document.ReleaseStatus
import org.migor.feedless.repository.RepositoryId
import java.util.*

class OnnxDocumentClassifierTest {

  private lateinit var classifier: OnnxDocumentClassifier

  @BeforeEach
  fun setUp() {
    classifier = OnnxDocumentClassifier()
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

    val result = classifier.classify(document)

    assertThat(result).isNotNull()
    assertThat(result.category).isNotEmpty()
    assertThat(result.probability).isBetween(0.0, 1.0)
  }

}
