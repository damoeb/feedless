package org.migor.feedless.classifier

import com.github.jfasttext.JFastText
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.migor.feedless.document.Document
import org.migor.feedless.document.DocumentClassifierModel
import org.migor.feedless.document.ReleaseStatus
import org.migor.feedless.repository.RepositoryId
import java.nio.file.Path
import java.util.*
import kotlin.io.path.writeLines

class FastTextDocumentClassifierTest {

  private lateinit var classifier: FastTextDocumentClassifier
  private lateinit var model: DocumentClassifierModel

  @BeforeEach
  fun setUp(@TempDir tempDir: Path) {
    classifier = FastTextDocumentClassifier()
    model = DocumentClassifierModel(trainModel(tempDir))
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

    val results = classifier.classify(document, model)

    assertThat(results).isNotEmpty()
    assertThat(results.first().category).isEqualTo("__label__tech")
    assertThat(results).allSatisfy { assertThat(it.probability).isBetween(0.0, 1.0) }
  }

  private fun trainModel(dir: Path): String {
    val trainingData = dir.resolve("train.txt")
    trainingData.writeLines(
      listOf(
        "__label__tech new laptop chip performance battery speed review",
        "__label__tech the processor delivers amazing speed and battery life",
        "__label__tech macbook pro review with the fastest chip",
        "__label__sports the team won the football match in the final minute",
        "__label__sports the striker scored two goals in the league game",
        "__label__sports tennis player wins the championship final",
      )
    )
    val output = dir.resolve("model").toString()
    JFastText().runCmd(
      arrayOf(
        "supervised", "-input", trainingData.toString(), "-output", output,
        "-epoch", "50", "-minCount", "1", "-thread", "1", "-verbose", "0"
      )
    )
    return "$output.bin"
  }
}
